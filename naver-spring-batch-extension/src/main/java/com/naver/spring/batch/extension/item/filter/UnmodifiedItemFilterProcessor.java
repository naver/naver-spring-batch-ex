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
public class UnmodifiedItemFilterProcessor<I, O> implements ItemProcessor<I, O>, ChunkStream<I> {
	private Logger log = LoggerFactory.getLogger(UnmodifiedItemFilterProcessor.class);

	@Override
	public O process(I item) throws Exception {
		return (O) item;
	}

	@Override
	public void createChunk(List<I> chunkItems) {
		log.debug("createChunk: {}", chunkItems);
	}

	@Override
	public void completeChunk() {
		log.debug("completeChunk");
	}
}