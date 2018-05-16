package com.naver.spring.batch.sample.job.sample2;

import com.naver.spring.batch.sample.job.AbstractJobTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MigrateUserJobConfig.class)
public class MigrateUserJobConfigTest extends AbstractJobTest {

	@Test
	public void migrateUserJob() throws Exception {
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
	}
}
