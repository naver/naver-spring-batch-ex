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