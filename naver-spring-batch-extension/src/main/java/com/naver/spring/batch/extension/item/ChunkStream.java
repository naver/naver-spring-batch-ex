package com.naver.spring.batch.extension.item;

import java.util.List;

/**
 * @author fomuo@navercorp.com
 */
public interface ChunkStream<I> {
	void createChunk(List<I> chunkItems);
	void completeChunk();
}
