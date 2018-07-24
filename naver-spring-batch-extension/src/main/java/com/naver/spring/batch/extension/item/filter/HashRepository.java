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
