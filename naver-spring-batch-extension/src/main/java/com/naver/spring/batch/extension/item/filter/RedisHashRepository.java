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

import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * redis 저장소에 hash 값을 저장하는 HashRepository 구현 객체
 *
 * @author yongkyu.lee
 * @since 1.1
 */
public class RedisHashRepository implements HashRepository {
	private final ValueOperations<String, String> valueOperations;

	private String keyNamespace = "nsbe#unmodifieditemfilter#";

	public RedisHashRepository(ValueOperations<String, String> valueOperations) {
		this.valueOperations = valueOperations;
	}

	/**
	 * redis 에 저장될 itemKey 의 namespace. default 'nsbe#unmodifieditemfilter#{itemKey}'
	 * redis key naming convention 을 따름.
	 *
	 * @param keyNamespace namespace
	 */
	public void setKeyNamespace(String keyNamespace) {
		this.keyNamespace = keyNamespace;
	}

	@Override
	public String getHashValue(String itemKey) {
		String key = keyNamespace + itemKey;
		return valueOperations.get(key);
	}

	@Override
	public void saveItemHashes(List<ItemHash> itemHashes) {
		long nowts = new Date().getTime();

		itemHashes.forEach(item -> {
			long timeout = item.getExpiry().getTime() - nowts;
			valueOperations.set(keyNamespace + item.getItemKey(), item.getItemHash(), timeout, TimeUnit.MICROSECONDS);
		});
	}
}
