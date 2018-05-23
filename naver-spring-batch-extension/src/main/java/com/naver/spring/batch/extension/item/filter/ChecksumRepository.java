package com.naver.spring.batch.extension.item.filter;

public interface ChecksumRepository {
	String getChecksum(String key);
}
