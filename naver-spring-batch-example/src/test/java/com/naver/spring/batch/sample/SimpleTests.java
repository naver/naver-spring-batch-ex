package com.naver.spring.batch.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.naver.spring.batch.extension.item.filter.HashIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class SimpleTests {

	@Test
	public void testSerialize() throws Exception {

		byte[] aa = BigInteger.valueOf(123).toByteArray();

		TestObject object = TestObject.of(1, "1", 123, "abc", ImmutableMap.of("mapkey1", "mapval1", "mapkey2", "mapval2"), Arrays.asList("arr1", "arr2"), aa);


		byte[] val = HashUtil.putHashSource(object);

		System.out.println(new String(val));
	}

	@AllArgsConstructor(staticName = "of")
	@Data
	static class TestObject {
		private int id1;
		private String id2;
		private int val1;
		private String val2;
		private Map<String, String> val3;
		@HashIgnore
		private List<String> val4;

		@HashIgnore
		private byte[] val6;

		public byte[] getVal6() {
			return val6;
		}
	}

	@Slf4j
	static class HashUtil {

		public static byte[] putHashSource(Object obj) throws Exception {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode jsonNode = mapper.createObjectNode();


			Set<String> hashIgnoreFields = Arrays.stream(obj.getClass().getDeclaredFields()).filter(p -> p.isAnnotationPresent(HashIgnore.class)).map(Field::getName).collect(Collectors.toSet());

			for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(obj.getClass())) {
				if ("class".equals(pd.getName()) ||
						Arrays.asList("id1", "id2").contains(pd.getName())) {
					continue;
				}

				String pdName = pd.getName();

				Object val = pd.getReadMethod().invoke(obj);

				HashIgnore annotation = AnnotationUtils.findAnnotation(pd.getReadMethod(), HashIgnore.class);

				if (annotation != null || hashIgnoreFields.contains(pdName)) {
					continue;
				}

				jsonNode.putPOJO(pdName, val);
			}

			return mapper.writeValueAsBytes(jsonNode);
		}

		private static void appendLog(StringBuilder logSb, Object...msgs) {
			if (logSb != null) {
				for (Object msg : msgs) {
					logSb.append(msg);
				}
			}
		}
	}
}
