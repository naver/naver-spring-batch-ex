package com.naver.spring.batch.extension.item.filter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumUnmodifiedItemChecker<T> implements UnmodifiedItemChecker<T> {
	private ChecksumRepository checksumRepository;
	private MessageDigest md;

	public void setChecksumRepository(ChecksumRepository checksumRepository) {
		this.checksumRepository = checksumRepository;
	}

	public void setMessageDigestAlgorithm(String algorithm) throws NoSuchAlgorithmException {
		this.md = MessageDigest.getInstance("md5");
	}

	@Override
	public boolean check(T item) {
		String key = generateKey(item);
		String checksum = checksumAsBase64(item);
		String storedChecksum = checksumRepository.getChecksum(key);

		return checksum.equals(storedChecksum);
	}

	private String generateKey(T item) {
		return "";
	}

	private String checksumAsBase64(T item) {
		return "";
	}
}
