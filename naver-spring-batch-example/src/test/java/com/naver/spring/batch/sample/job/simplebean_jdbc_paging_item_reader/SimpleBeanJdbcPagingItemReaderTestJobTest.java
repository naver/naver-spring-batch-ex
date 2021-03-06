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
package com.naver.spring.batch.sample.job.simplebean_jdbc_paging_item_reader;

import com.naver.spring.batch.sample.job.AbstractJobTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @(#) SimpleJdbcPagingItemReaderTestJobConfig.class $version 2018. 07. 04
 * <p>
 * Copyright 2018 NAVER Corp. All rights Reserved.
 * NAVER PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
@SpringBootTest(classes = SimpleBeanJdbcPagingItemReaderTestJobConfig.class)
public class SimpleBeanJdbcPagingItemReaderTestJobTest extends AbstractJobTest {

	@Test
	public void simpleJdbcPagingItemReaderTestJob() throws Exception {
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
	}
}
