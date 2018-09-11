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
package com.naver.spring.batch.sample.job.unmodified_filter;

import com.naver.spring.batch.sample.job.AbstractJobTest;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = UnmodifiedFilterJobConfig.class)
//@TestPropertySource(properties = {
//		"logging.level.com.naver.spring.batch.extension.item.filter=debug",
//		"logging.level.org.springframework.jdbc=debug"
//})
public class UnmodifiedFilterJobTest extends AbstractJobTest {
	@Test
	public void unmodifiedFilter_test() throws Exception {
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
	}

	@Test
	public void unmodifiedFilterJob_skip_test() throws Exception {
		JobParametersBuilder jpBuilder = new JobParametersBuilder();
		jpBuilder.addString("UnmodifiedItemFilter-skip", "true");
		jpBuilder.addLong("random", RandomUtils.nextLong());

		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jpBuilder.toJobParameters());
		Assert.assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
	}
}
