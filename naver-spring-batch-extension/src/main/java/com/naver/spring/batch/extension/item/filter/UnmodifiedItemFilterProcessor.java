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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>
 * {@link com.naver.spring.batch.extension.item.ListenerSupportCompositeItemProcessor} 를 통해 연결된
 * Processor chain 의 적당한 위치 (보통은 가장 마지막) 에 추가하여 변경되지 않은 Item 에 대해 filter 처리 한다.
 * </p>
 *
 * <p>
 * JobParameter 'UnmodifiedItemFilter-skip=true' 를 통해 모든 Item 을 filtering 하지 않도록 할 수 있다.
 * 이때 모든 Item 의 hash 값은 다시 계산되어 저장되므로 이후 JobParameter 를 제거하더라도 filtering 이 정상적으로 처리된다.
 * </p>
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public class UnmodifiedItemFilterProcessor<T> extends ChunkListenerSupport implements ItemProcessor<T, T>, InitializingBean {
	private Logger log = LoggerFactory.getLogger(UnmodifiedItemFilterProcessor.class);

	private UnmodifiedItemChecker<T> checker;
	private boolean skipUnmodifiedItemCheck = false;

	public void setChecker(UnmodifiedItemChecker<T> checker) {
		this.checker = checker;
	}

	@Override
	public void beforeChunk(ChunkContext context) {
		String skipUnmodifiedItemCheck = (String)context.getStepContext().getJobParameters().get("UnmodifiedItemFilter-skip");
		this.skipUnmodifiedItemCheck = Boolean.parseBoolean(skipUnmodifiedItemCheck);

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

			if (!skipUnmodifiedItemCheck) {
				return null;
			}
		}

		return item;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(checker, "checker must not be null");
	}
}
