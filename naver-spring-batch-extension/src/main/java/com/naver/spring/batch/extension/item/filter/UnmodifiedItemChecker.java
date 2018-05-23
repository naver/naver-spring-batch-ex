package com.naver.spring.batch.extension.item.filter;

/**
 * 변경되지 않는 아이템인지 체크함.
 * @param <T>
 */
public interface UnmodifiedItemChecker<T> {
	boolean check(T item);
}
