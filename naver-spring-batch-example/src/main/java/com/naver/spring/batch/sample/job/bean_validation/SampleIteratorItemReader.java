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
package com.naver.spring.batch.sample.job.bean_validation;

import com.naver.spring.batch.sample.domain.People3;
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
	public People3 read() throws Exception {
		if (iter.hasNext()) {
			return iter.next();
		}

		return null;
	}
}
