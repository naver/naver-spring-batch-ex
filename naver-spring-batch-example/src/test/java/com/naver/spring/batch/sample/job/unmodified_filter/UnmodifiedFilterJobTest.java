package com.naver.spring.batch.sample.job.unmodified_filter;

import com.naver.spring.batch.sample.job.AbstractJobTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UnmodifiedFilterJobConfig.class)
public class UnmodifiedFilterJobTest extends AbstractJobTest {
	@Test
	public void unmodifiedFilterJob() throws Exception {
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
	}
}
