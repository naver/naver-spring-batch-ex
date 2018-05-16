package com.naver.spring.batch.extension.item;

import java.util.List;

public interface ChunkStream<I> {
	void createChunk(List<I> chunkItems);
	void completeChunk();
}
