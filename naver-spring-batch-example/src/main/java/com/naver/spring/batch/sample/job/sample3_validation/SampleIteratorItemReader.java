package com.naver.spring.batch.sample.job.sample3_validation;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.UnexpectedInputException;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SampleIteratorItemReader implements ItemReader<People3> {
	private Iterator<People3> iter;

	public SampleIteratorItemReader() {
		try {
			List<People3> list = Arrays.asList(
					People3.of(1, "lee1", 10, "yk1@naver.com", DateUtils.parseDate("1983-04-09", "yyyy-MM-dd"), "010-2933-2095", "http://wanzargen.tistory.com/28"),
					People3.of(2, "lee2", 20, "yk2@naver.com", DateUtils.parseDate("1983-04-09", "yyyy-MM-dd"), "010-2933-2094", "http://wanzargen.tistory.com/16"),
					People3.of(3, "lee3", 30, "yk3@naver.com", DateUtils.parseDate("1983-04-09", "yyyy-MM-dd"), "010-2933-2093", "http://wanzargen.tistory.com/15")
			);

			iter = list.iterator();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public People3 read() throws Exception, UnexpectedInputException, org.springframework.batch.item.ParseException, NonTransientResourceException {
		if (iter.hasNext()) {
			return iter.next();
		}

		return null;
	}
}
