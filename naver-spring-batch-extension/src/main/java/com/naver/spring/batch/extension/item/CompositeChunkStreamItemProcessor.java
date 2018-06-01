package com.naver.spring.batch.extension.item;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.List;

public class CompositeChunkStreamItemProcessor<I, O> extends ChunkListenerSupport implements ItemProcessor<I, O>, InitializingBean {

	private List<? extends ItemProcessor<?, ?>> delegates;

	@Override
	public void beforeChunk(ChunkContext context) {
		for (ItemProcessor<?, ?> delegate : delegates) {
			if (delegate instanceof ChunkListener) {
				((ChunkListener)delegate).beforeChunk(context);
			}
		}
	}

	@Override
	public void afterChunk(ChunkContext context) {
		for (ItemProcessor<?, ?> delegate : delegates) {
			if (delegate instanceof ChunkListener) {
				((ChunkListener)delegate).afterChunk(context);
			}
		}
	}

	@Override
	public void afterChunkError(ChunkContext context) {
		for (ItemProcessor<?, ?> delegate : delegates) {
			if (delegate instanceof ChunkListener) {
				((ChunkListener)delegate).afterChunkError(context);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public O process(I item) throws Exception {
		Object result = item;

		for (ItemProcessor<?, ?> delegate : delegates) {
			if (result == null) {
				return null;
			}

			result = processItem(delegate, result);
		}
		return (O) result;
	}

	/*
	 * Helper method to work around wildcard capture compiler error: see http://docs.oracle.com/javase/tutorial/java/generics/capture.html
	 * The method process(capture#1-of ?) in the type ItemProcessor<capture#1-of ?,capture#2-of ?> is not applicable for the arguments (Object)
	 */
	@SuppressWarnings("unchecked")
	private <T> Object processItem(ItemProcessor<T, ?> processor, Object input) throws Exception {
		return processor.process((T) input);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(delegates, "The 'delegates' may not be null");
		Assert.notEmpty(delegates, "The 'delegates' may not be empty");
	}

	public void setDelegates(List<? extends ItemProcessor<?, ?>> delegates) {
		this.delegates = delegates;
	}
}
