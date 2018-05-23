/**
 * @(#) UnmodifiedItemFilterProcessor.class $version 2018. 05. 13
 * <p>
 * Copyright 2018 NAVER Corp. All rights Reserved.
 * NAVER PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.naver.spring.batch.extension.item.filter;

import com.naver.spring.batch.extension.item.ChunkStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

/**
 * UnmodifiedItemFilterProcessor 
 *
 * @author 스포츠_개발 (dl_sports_sweng@navercorp.com)
 */
public class UnmodifiedItemFilterProcessor<T> implements ItemProcessor<T, T>, ChunkStream<T> {
	private Logger log = LoggerFactory.getLogger(UnmodifiedItemFilterProcessor.class);

	private UnmodifiedItemChecker<T> checker;

	public void setChecker(UnmodifiedItemChecker<T> checker) {
		this.checker = checker;
	}

	@Override
	public T process(T item) throws Exception {
		if (checker.check(item)) {
			return null;
		}

		return item;
	}

	@Override
	public void createChunk(List<T> chunkItems) {
		log.debug("createChunk: {}", chunkItems);
	}

	@Override
	public void completeChunk() {
		log.debug("completeChunk");
	}
}