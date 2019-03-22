/*
Copyright 2018 NAVER Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.naver.spring.batch.extension.item.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Item 에 대한 hash 값을 구한 후 해당 값을 통해 Item 의 변경 여부를 확인 한다.
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public class HashUnmodifiedItemChecker<T> extends ChunkListenerSupport implements UnmodifiedItemChecker<T>, InitializingBean {
	private Logger log = LoggerFactory.getLogger(HashUnmodifiedItemChecker.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	private HashRepository hashRepository;
	private MessageDigest md;
	private List<String> keyPropertyNames;
	private List<String> ignorePropertyNames;
	private List<PropertyDescriptor> keyPropertyDescriptors;
	private List<PropertyDescriptor> hashPropertyDescriptors;

	private long expiry;
	private List<ItemHash> chunkItemHashes = new ArrayList<>();
	private String keyPrefix;

	public void setHashRepository(HashRepository hashRepository) {
		this.hashRepository = hashRepository;
	}

	/**
	 * <p>
	 * hash 값 계산에 사용될 hash 함수. default MD5
	 * </p>
	 * <ul>
	 * <li>{@code MD5}</li>
	 * <li>{@code SHA-1}</li>
	 * <li>{@code SHA-256}</li>
	 * </ul>
	 * @param algorithm algorithm name
	 * @throws NoSuchAlgorithmException {@link NoSuchAlgorithmException}
	 */
	public void setHashAlgorithm(String algorithm) throws NoSuchAlgorithmException {
		this.md = MessageDigest.getInstance(algorithm);
	}

	/**
	 * T item 의 키로 사용되는 property names
	 *
	 * @param keyPropertyNames item 에 대한 hash 값 저장시 키로 사용될 property names
	 */
	public void setKeyPropertyNames(List<String> keyPropertyNames) {
		this.keyPropertyNames = keyPropertyNames;
	}

	/**
	 * Item 에 대한 hash값 생성시 제외할 property names
	 *
	 * <code>@HashIgnore</code> 로 지정된 프로퍼티와 함께 제외 된다.
	 *
	 * @param ignorePropertyNames item 에 대한 hash 값 생성시 제외할 property names
	 */
	public void setIgnorePropertyNames(List<String> ignorePropertyNames) {
		this.ignorePropertyNames = ignorePropertyNames;
	}

	/**
	 * hash 값 만료시간 (초), default 100 years
	 * @param expiry hash 값 만료시간 (초)
	 */
	public void setExpiry(int expiry) {
		this.setExpiry(expiry, TimeUnit.SECONDS);
	}

	/**
	 * hash 값 만료시간
	 *
	 * @param expiry hash 값 만료시간. (timeUnit 단위의 값)
	 * @param timeUnit expiry 에 대한 시간 단위
	 */
	public void setExpiry(int expiry, TimeUnit timeUnit) {
		this.expiry = timeUnit.toMillis(expiry);
	}

	/**
	 * hashkey string 생성시 사용될 prefix. default empty string
	 * @param keyPrefix hashkey string 생성시 사용될 prefix
	 */
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	@Override
	public void afterChunk(ChunkContext context) {
		if (!chunkItemHashes.isEmpty()) {
			hashRepository.saveItemHashes(chunkItemHashes);
			chunkItemHashes.clear();
		}
	}

	@Override
	public boolean check(T item) throws Exception {
		initPropertyDescriptors(item);

		String key = makeHashKey(item);
		String hashSource = makeHashSource(item);
		String hashValue = Base64.getEncoder().encodeToString(this.md.digest(hashSource.getBytes()));
		String storedHashValue = hashRepository.getHashValue(key);

		if (log.isDebugEnabled()) {
			log.debug("\n\tHash Key: {}\n\tHash Source: {}\n\tHash Value: {}\n\tStored Hash Value: {}",
					key, hashSource, hashValue, storedHashValue);
		}

		boolean eq = hashValue.equals(storedHashValue);

		if (!eq) {
			long ts = new Date().getTime();

			if (expiry > 0) {
				ts += expiry;
			} else {
				ts += 3153600000000L; //100년
			}
			chunkItemHashes.add(new ItemHash(key, hashValue, new Date(ts)));
		}

		return eq;
	}

	private String makeHashKey(T item) throws InvocationTargetException, IllegalAccessException {
		StringBuilder sb =  new StringBuilder();

		if (keyPrefix != null) {
			sb.append(keyPrefix).append('.');
		}

		sb.append(item.getClass().getSimpleName());

		for (PropertyDescriptor propertyDescriptor : keyPropertyDescriptors) {
			Method method = propertyDescriptor.getReadMethod();
			Object value = method.invoke(item);

			sb.append('-').append(value);
		}

		return sb.toString();
	}

	private void initPropertyDescriptors(T item) {
		if (this.keyPropertyDescriptors == null) {

			Set<String> hashKeyFields = Arrays.stream(item.getClass().getDeclaredFields())
					.filter(p -> p.isAnnotationPresent(HashKey.class))
					.map(Field::getName)
					.collect(Collectors.toSet());

			if (this.keyPropertyNames != null && !this.keyPropertyNames.isEmpty()) {
				hashKeyFields.addAll(this.keyPropertyNames);
			}

			Set<String> hashIgnoreFields = Arrays.stream(item.getClass().getDeclaredFields())
					.filter(p -> p.isAnnotationPresent(HashIgnore.class))
					.map(Field::getName)
					.collect(Collectors.toSet());

			if (this.ignorePropertyNames != null && !this.ignorePropertyNames.isEmpty()) {
				hashIgnoreFields.addAll(this.ignorePropertyNames);
			}

			PropertyDescriptor[] pdsAll = BeanUtils.getPropertyDescriptors(item.getClass());
			List<PropertyDescriptor> hashKeyPds = new ArrayList<>(hashKeyFields.size());
			List<PropertyDescriptor> hashPds = new ArrayList<>(pdsAll.length);

			for (PropertyDescriptor pd : pdsAll) {
				if ("class".equals(pd.getName())) {
					continue;
				}

				HashKey annHashKey = AnnotationUtils.findAnnotation(pd.getReadMethod(), HashKey.class);

				if (annHashKey != null || hashKeyFields.contains(pd.getName())) {
					Assert.isTrue(BeanUtils.isSimpleValueType(pd.getPropertyType()),
							"'" + pd.getName() + "' value type must be simple value");

					hashKeyPds.add(pd);
					continue;
				}

				HashIgnore annotation = AnnotationUtils.findAnnotation(pd.getReadMethod(), HashIgnore.class);

				if (annotation != null || hashIgnoreFields.contains(pd.getName())) {
					continue;
				}

				hashPds.add(pd);
			}

			Assert.notEmpty(hashKeyPds, "Properties for hash key must not be empty");
			Assert.notEmpty(hashPds, "Properties for hash source must not be empty");

			hashKeyPds.sort(Comparator.comparing(PropertyDescriptor::getName));

			this.keyPropertyDescriptors = Collections.unmodifiableList(hashKeyPds);
			this.hashPropertyDescriptors = Collections.unmodifiableList(hashPds);
		}
	}

	private String makeHashSource(T item) throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
		ObjectNode jsonNode = mapper.createObjectNode();

		for (PropertyDescriptor pd : this.hashPropertyDescriptors) {
			Object val = pd.getReadMethod().invoke(item);
			jsonNode.putPOJO(pd.getName(), val);
		}

		return jsonNode.toString();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(hashRepository, "hashRepository must not be null");

		if (this.md == null) {
			this.md = MessageDigest.getInstance("md5");
		}
	}
}
