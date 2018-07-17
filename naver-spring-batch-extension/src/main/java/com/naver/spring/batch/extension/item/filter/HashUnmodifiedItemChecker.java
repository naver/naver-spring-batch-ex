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
import java.util.stream.Collectors;

public class HashUnmodifiedItemChecker<T> extends ChunkListenerSupport implements UnmodifiedItemChecker<T>, InitializingBean {
	private Logger log = LoggerFactory.getLogger(HashUnmodifiedItemChecker.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	private HashRepository hashRepository;
	private MessageDigest md;
	private List<String> keyPropertyNames;
	private int expiry;
	private List<ItemHash> chunkItemHashes = new ArrayList<>();
	private String keyPrefix;

	public void setHashRepository(HashRepository hashRepository) {
		this.hashRepository = hashRepository;
	}

	public void setHashAlgorithm(String algorithm) throws NoSuchAlgorithmException {
		this.md = MessageDigest.getInstance(algorithm);
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	/**
	 * T item 의 키로 사용되는 property names
	 * @param keyPropertyNames
	 */
	public void setKeyPropertyNames(List<String> keyPropertyNames) {
		this.keyPropertyNames = keyPropertyNames;
	}

	/**
	 * hash 값 만료시간 (초), default 만료 없음
	 * @param expiry
	 */
	public void setExpiry(int expiry) {
		this.expiry = expiry;
	}

	@Override
	public void beforeChunk(ChunkContext context) {
		if (keyPrefix == null) {
			String stepName = context.getStepContext().getStepName();

			if (stepName.length() > 10) {
				this.keyPrefix = stepName.substring(0, 10) + "_";
			} else {
				this.keyPrefix = stepName + "_";
			}
		}
	}

	@Override
	public void afterChunk(ChunkContext context) {
		hashRepository.saveItemHashes(chunkItemHashes);
		chunkItemHashes.clear();
	}

	@Override
	public boolean check(T item) {
		try {
			String key = generateKey(item);

			String hashValue = generateHashValue(item);
			String storedHashValue = hashRepository.getHashValue(key);

			if (log.isDebugEnabled()) {
				log.debug("item hash for '{}', generated value: '{}', stored value: '{}'",
						key, hashValue, (storedHashValue != null) ? storedHashValue : "null");
			}

			boolean eq = hashValue.equals(storedHashValue);

			if (!eq) {
				long ts = new Date().getTime();

				if (expiry > 0) {
					ts += (expiry * 1000);
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


	private String generateKey(T item) throws InvocationTargetException, IllegalAccessException {
		StringBuilder sb = new StringBuilder(keyPrefix);

		for (String keyProperty : keyPropertyNames) {
			PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(item.getClass(), keyProperty);

			Assert.isTrue(BeanUtils.isSimpleValueType(propertyDescriptor.getPropertyType()),
					keyProperty + " value type must be simple value");

			Method method = propertyDescriptor.getReadMethod();
			Object value = method.invoke(item);

			sb.append(value);
		}

		return sb.toString();
	}

	private String generateHashValue(T item) throws InvocationTargetException, IllegalAccessException, JsonProcessingException {
		ObjectNode jsonNode = mapper.createObjectNode();

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

			Object val = pd.getReadMethod().invoke(item);
			jsonNode.putPOJO(pd.getName(), val);
		}

		if (log.isDebugEnabled()) {
			log.debug("hash inputs as json: {}", jsonNode.toString() );
		}

		return Base64.getEncoder().encodeToString(this.md.digest(mapper.writeValueAsBytes(jsonNode)));
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