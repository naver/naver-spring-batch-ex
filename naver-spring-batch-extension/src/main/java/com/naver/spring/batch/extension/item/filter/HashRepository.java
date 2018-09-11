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

import java.util.List;

/**
 * {@link HashUnmodifiedItemChecker} 를 통해 계산된 hash 값을 저장하기 위한 Repository interface
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public interface HashRepository {
	/**
	 * itemKey 를 통해 저장되어 있는 hash값이 있다면 리턴한다.
	 *
	 * @param itemKey Item 의 keyProperties 로 부터 생성된 키값
	 * @return String (보통 base64) 로 인코딩된 hash 값
	 */
	String getHashValue(String itemKey);

	/**
	 * 생성된 Item hash 정보 저장
	 *
	 * @param itemHashes 저장할 Item hash 정보
	 */
	void saveItemHashes(List<ItemHash> itemHashes);
}
