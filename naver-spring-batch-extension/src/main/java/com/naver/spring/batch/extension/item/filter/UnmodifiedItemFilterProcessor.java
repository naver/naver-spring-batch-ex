/**
 * @(#) UnmodifiedItemFilterProcessor.class $version 2018. 05. 13
 * <p>
 * Copyright 2018 NAVER Corp. All rights Reserved.
 * NAVER PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.naver.spring.batch.extension.item.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;

/**
 * UnmodifiedItemFilterProcessor 
 *
 * @author 스포츠_개발 (dl_sports_sweng@navercorp.com)
 */
public class UnmodifiedItemFilterProcessor<T> extends ChunkListenerSupport implements ItemProcessor<T, T>, InitializingBean {
	private Logger log = LoggerFactory.getLogger(UnmodifiedItemFilterProcessor.class);

	private UnmodifiedItemChecker<T> checker;

	public void setChecker(UnmodifiedItemChecker<T> checker) {
		this.checker = checker;
	}

	@Override
	public void beforeChunk(ChunkContext context) {
		if (checker instanceof ChunkListener) {
			((ChunkListener)checker).beforeChunk(context);
		}
	}

	@Override
	public void afterChunk(ChunkContext context) {
		if (checker instanceof ChunkListener) {
			((ChunkListener)checker).afterChunk(context);
		}
	}

	@Override
	public T process(T item) throws Exception {
		if (checker.check(item)) {
			if (log.isDebugEnabled()) {
				log.debug("Item filtered : {}", item );
			}
			return null;
		}

		return item;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}