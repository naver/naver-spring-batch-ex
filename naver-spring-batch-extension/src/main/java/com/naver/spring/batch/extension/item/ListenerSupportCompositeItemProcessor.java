package com.naver.spring.batch.extension.item;

import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.List;

public class ListenerSupportCompositeItemProcessor<I, O> implements ItemProcessor<I, O>, InitializingBean,
		StepExecutionListener, ChunkListener, ItemWriteListener<O> {

	private List<? extends ItemProcessor<?, ?>> delegates;

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

	@Override
	public void beforeStep(StepExecution stepExecution) {
		for (ItemProcessor<?, ?> delegate : delegates) {
			if (delegate instanceof StepExecutionListener) {
				((StepExecutionListener)delegate).beforeStep(stepExecution);
			}
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		ExitStatus exitStatus = stepExecution.getExitStatus();

		for (ItemProcessor<?, ?> delegate : delegates) {
			if (delegate instanceof StepExecutionListener) {
				ExitStatus tmp = ((StepExecutionListener)delegate).afterStep(stepExecution);
				if (tmp != null) {
					exitStatus = tmp;
				}
			}
		}
		return exitStatus;
	}

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
	public void beforeWrite(List<? extends O> items) {
		for (ItemProcessor<?, ?> delegate : delegates) {
			if (delegate instanceof ItemWriteListener) {
				//noinspection unchecked
				((ItemWriteListener)delegate).beforeWrite(items);
			}
		}
	}

	@Override
	public void afterWrite(List<? extends O> items) {
		for (ItemProcessor<?, ?> delegate : delegates) {
			if (delegate instanceof ItemWriteListener) {
				//noinspection unchecked
				((ItemWriteListener)delegate).afterWrite(items);
			}
		}
	}

	@Override
	public void onWriteError(Exception exception, List<? extends O> items) {
		for (ItemProcessor<?, ?> delegate : delegates) {
			if (delegate instanceof ItemWriteListener) {
				//noinspection unchecked
				((ItemWriteListener)delegate).onWriteError(exception, items);
			}
		}
	}
}
