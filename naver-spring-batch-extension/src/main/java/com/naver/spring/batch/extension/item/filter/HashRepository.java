package com.naver.spring.batch.extension.item.filter;

import java.util.List;

public interface HashRepository {
	String getHashValue(String itemKey);

	void saveItemHashes(List<ItemHash> itemHashes);
}
