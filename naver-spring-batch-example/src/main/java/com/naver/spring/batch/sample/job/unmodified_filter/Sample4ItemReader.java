package com.naver.spring.batch.sample.job.unmodified_filter;

import com.naver.spring.batch.sample.domain.Sample4;
import org.springframework.batch.item.ItemReader;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Sample4ItemReader implements ItemReader<Sample4> {
	private Iterator<Sample4> iterator;

	public Sample4ItemReader() {
		Date now = new Date();

		Sample4 s_1 = Sample4.of(1, "a", 1.1f, 10, "val1", now);
		Sample4 s_2 = Sample4.of(2, "b", 2.2f, 20, "val2", now);
		Sample4 s_3 = Sample4.of(3, "c", 3.3f, 30, "val3", now);
		Sample4 s_4 = Sample4.of(4, "d", 4.4f, 40, "val4", now);
		Sample4 s_5 = Sample4.of(5, "e", 5.5f, 50, "val5", now);

		List<Sample4> samples = Arrays.asList(s_1, s_2, s_3, s_4, s_5);
		iterator = samples.iterator();
	}

	@Override
	public Sample4 read() throws Exception {
		if (!iterator.hasNext())
			return null;

		return iterator.next();
	}
}
