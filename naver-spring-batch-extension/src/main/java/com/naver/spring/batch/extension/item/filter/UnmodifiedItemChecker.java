package com.naver.spring.batch.extension.item.filter;

/**
 * 변경되지 않는 아이템인지 체크함.
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public interface UnmodifiedItemChecker<T> {
	/**
	 * item에 대해 변경 여부를 판단한다.
	 *
	 * @param item 변경 여부를 확인할 item
	 * @return true - 변경되지 않음, false - 변경되었거나 처음 처리됨
	 */
	boolean check(T item);
}
