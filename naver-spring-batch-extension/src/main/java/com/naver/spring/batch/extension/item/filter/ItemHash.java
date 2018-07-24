package com.naver.spring.batch.extension.item.filter;

import java.util.Date;

/**
 * 저장될 hash값 객체
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public class ItemHash {
	private String itemKey;
	private String itemHash;
	private Date expiry;

	public ItemHash() {
	}
	public ItemHash(String itemKey, String itemHash, Date expiry) {
		this.itemKey = itemKey;
		this.itemHash = itemHash;
		this.expiry = expiry;
	}

	public String getItemKey() {
		return itemKey;
	}

	public void setItemKey(String itemKey) {
		this.itemKey = itemKey;
	}

	public String getItemHash() {
		return itemHash;
	}

	public void setItemHash(String itemHash) {
		this.itemHash = itemHash;
	}

	public Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}
}
