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

	@Override
	public void beforeChunk(ChunkContext context) {
		if (keyPrefix == null) {
			this.keyPrefix = context.getStepContext().getStepName();
		}
	}

	@Override
	public void afterChunk(ChunkContext context) {
		if (!chunkItemHashes.isEmpty()) {
			hashRepository.saveItemHashes(chunkItemHashes);
			chunkItemHashes.clear();
		}
	}

	@Override
	public boolean check(T item) {
		initKeyPropertyDescriptors(item);
		initHashPropertyDescriptors(item);

		try {
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
		} catch (Exception e) {
			log.error("Error HashUnmodifiedItemChecker", e);
		}

		return false;
	}


	private void initKeyPropertyDescriptors(T item) {
		if (this.keyPropertyDescriptors == null) {
			List<PropertyDescriptor> pds = new ArrayList<>(keyPropertyNames.size());

			for (String keyProperty : keyPropertyNames) {
				PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(item.getClass(), keyProperty);

				Assert.notNull(propertyDescriptor, "'" + keyProperty + "' is invalid property name");
				Assert.isTrue(BeanUtils.isSimpleValueType(propertyDescriptor.getPropertyType()),
						"'" + keyProperty + "' value type must be simple value");

				pds.add(propertyDescriptor);
			}

			this.keyPropertyDescriptors = Collections.unmodifiableList(pds);
		}
	}

	private String makeHashKey(T item) throws InvocationTargetException, IllegalAccessException {
		StringBuilder sb = new StringBuilder(keyPrefix);

		for (PropertyDescriptor propertyDescriptor : keyPropertyDescriptors) {
			Method method = propertyDescriptor.getReadMethod();
			Object value = method.invoke(item);

			sb.append('-').append(value);
		}

		return sb.toString();
	}

	private void initHashPropertyDescriptors(T item) {
		if (this.hashPropertyDescriptors == null) {
			List<PropertyDescriptor> pds = new ArrayList<>();

			Set<String> hashIgnoreFields = Arrays.stream(item.getClass().getDeclaredFields())
					.filter(p -> p.isAnnotationPresent(HashIgnore.class))
					.map(Field::getName)
					.collect(Collectors.toSet());

			for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(item.getClass())) {
				if ("class".equals(pd.getName()) || keyPropertyNames.contains(pd.getName())) {
					continue;
				}

				HashIgnore annotation = AnnotationUtils.findAnnotation(pd.getReadMethod(), HashIgnore.class);

				if (annotation != null || hashIgnoreFields.contains(pd.getName())) {
					continue;
				}

				pds.add(pd);
			}

			this.hashPropertyDescriptors = Collections.unmodifiableList(pds);
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
		Assert.notEmpty(keyPropertyNames, "keyPropertyNames must not be empty");

		if (this.md == null) {
			this.md = MessageDigest.getInstance("md5");
		}
	}
}
