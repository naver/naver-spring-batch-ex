package com.naver.spring.batch.extension.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author fomuo@navercorp.com
 */
public class CompositeChunkStreamItemProcessor<I, O> implements ItemProcessor<I, O>, ChunkStream<I>, InitializingBean {
	private Logger log = LoggerFactory.getLogger(CompositeChunkStreamItemProcessor.class);

	private List<? extends ItemProcessor<?, ?>> delegates;
	private ChunkContext chunkContext;
	private boolean chunkProcessing = false;

	@BeforeChunk
	private void beforeChunk(ChunkContext chunkContext) {
		this.chunkContext = chunkContext;
	}

	@AfterChunk
	private void afterChunk(ChunkContext context) {
		log.debug(context.toString());
		chunkProcessing = false;
		completeChunk();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void createChunk(List<I> chunkItems) {
		for (ItemProcessor<?, ?> delegate : delegates) {
			if (delegate instanceof ChunkStream) {
				((ChunkStream<I>)delegate).createChunk(chunkItems);
			}
		}
	}

	@Override
	public void completeChunk() {
		for (ItemProcessor<?, ?> delegate : delegates) {
			if (delegate instanceof ChunkStream) {
				((ChunkStream)delegate).completeChunk();
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public O process(I item) throws Exception {
		if (!chunkProcessing) {
			chunkProcessing = true;

			if (chunkContext != null && chunkContext.hasAttribute("INPUTS")) {
				Chunk<I> inputs = (Chunk<I>)chunkContext.getAttribute("INPUTS");
				createChunk(inputs.getItems());
			}
		}

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
