package com.naver.spring.batch.sample.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Slf4j
public class LoggingItemWriter<T> implements ItemWriter<T> {

	@Override
	public void write(List<? extends T> items) throws Exception {
		log.info("==========Write Items Begin=====");

		for (int i = 0; i < items.size(); i++) {
			log.info("{} - {}", i + 1, items.get(i));
		}

		log.info("==========Write Items End=====");
	}
}
