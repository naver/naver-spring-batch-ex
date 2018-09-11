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
