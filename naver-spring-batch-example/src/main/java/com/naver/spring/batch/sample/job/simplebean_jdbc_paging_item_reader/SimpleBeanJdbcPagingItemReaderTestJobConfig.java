package com.naver.spring.batch.sample.job.simplebean_jdbc_paging_item_reader;

import com.google.common.collect.ImmutableMap;
import com.naver.spring.batch.extension.item.database.SimpleBeanJdbcPagingItemReader;
import com.naver.spring.batch.sample.config.LoggingItemWriter;
import com.naver.spring.batch.sample.domain.Player;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SimpleBeanJdbcPagingItemReaderTestJobConfig {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;

	@Autowired
	public SimpleBeanJdbcPagingItemReaderTestJobConfig(JobBuilderFactory jobBuilderFactory,
	                                                   StepBuilderFactory stepBuilderFactory,
	                                                   DataSource dataSource) {
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		this.dataSource = dataSource;
	}

	@Bean
	public Job simpleJdbcPagingItemReaderTestJob() throws Exception {
		return jobBuilderFactory.get("simpleJdbcPagingItemReaderTestJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
	}

	private Step step1() throws Exception {
		return stepBuilderFactory.get("step1")
				.<Player, Player> chunk(3)
				.reader(reader())
				.writer(new LoggingItemWriter<>())
				.build();
	}

	private ItemReader<Player> reader() throws Exception {
		SimpleBeanJdbcPagingItemReader<Player> itemReader = new SimpleBeanJdbcPagingItemReader<>(Player.class);
		itemReader.setDataSource(dataSource);
		itemReader.setPageSize(3);
		itemReader.setSortKeys(ImmutableMap.of("id", Order.ASCENDING));

		itemReader.afterPropertiesSet();

		return itemReader;
	}
}