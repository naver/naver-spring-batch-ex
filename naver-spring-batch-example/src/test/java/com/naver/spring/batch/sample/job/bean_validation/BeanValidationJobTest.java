package com.naver.spring.batch.sample.job.bean_validation;

import com.naver.spring.batch.sample.job.AbstractJobTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = "classpath:jobs/BeanValidationJobConfig.xml")
@SpringBootTest
public class BeanValidationJobTest extends AbstractJobTest {

	@Test
	public void beanValidationJob() throws Exception {
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
	}
}
