package com.naver.spring.batch.extension.item.filter;

public class UnsupportedDatabaseException extends RuntimeException {
	
	public UnsupportedDatabaseException() {
	}
	public UnsupportedDatabaseException(String message) {
		super(message);
	}
	public UnsupportedDatabaseException(String message, Throwable cause) {
		super(message, cause);
	}
	public UnsupportedDatabaseException(Throwable cause) {
		super(cause);
	}
}
