package com.naver.spring.batch.extension.test.sample;

import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author fomuo@navercorp.com
 */
@Configuration
public class TestConfiguration {
	@Bean
	public JobLauncherTestUtils jobLauncherTestUtils() {
		return jobLauncherTestUtils();
	}
}
