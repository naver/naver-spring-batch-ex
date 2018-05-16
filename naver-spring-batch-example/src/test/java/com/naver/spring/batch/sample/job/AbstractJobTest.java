package com.naver.spring.batch.sample.job;

import com.naver.spring.batch.sample.config.BatchConfig;
import org.junit.runner.RunWith;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@AutoConfigureJdbc
@Import({ BatchConfig.class, AbstractJobTest.TestConfig.class })
@TestPropertySource(properties = "spring.batch.job.enabled=false")
public abstract class AbstractJobTest {
	@Autowired
	protected JobLauncherTestUtils jobLauncherTestUtils;

	@TestConfiguration
	static class TestConfig {
		@Bean
		public JobLauncherTestUtils jobLauncherTestUtils() {
			return new JobLauncherTestUtils();
		}
	}
}
