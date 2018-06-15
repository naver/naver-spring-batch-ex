package com.naver.spring.batch.sample.job.sample4;

import com.naver.spring.batch.extension.item.ListenerSupportCompositeItemProcessor;
import com.naver.spring.batch.extension.item.filter.HashUnmodifiedItemChecker;
import com.naver.spring.batch.extension.item.filter.JdbcHashRepository;
import com.naver.spring.batch.extension.item.filter.UnmodifiedItemFilterProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class UnmodifiedFilterJobConfig {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;
	private final PlatformTransactionManager transactionManager;

	@Autowired
	public UnmodifiedFilterJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource, PlatformTransactionManager transactionManager) {
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		this.dataSource = dataSource;
		this.transactionManager = transactionManager;
	}

	@Bean
	public Job unmodifiedFilterJob() throws Exception {
		return jobBuilderFactory.get("unmodifiedFilterJob")
				.incrementer(new RunIdIncrementer())
				.flow(unmodifiedFilterStep())
				.end()
				.build();
	}

	private Step unmodifiedFilterStep() throws Exception {
		return stepBuilderFactory.get("unmodifiedFilterStep")
				.<Sample4, Sample4> chunk(2)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}

	private ItemReader<Sample4> reader() {
		return new Sample4ItemReader();
	}

	private ItemProcessor<Sample4, Sample4> processor() throws Exception {
		HashUnmodifiedItemChecker<Sample4> checker = new HashUnmodifiedItemChecker<>();
		checker.setHashRepository(new JdbcHashRepository(dataSource, transactionManager));
		checker.setKeyPropertyNames(Arrays.asList("idInt", "idStr"));
		checker.afterPropertiesSet();

		UnmodifiedItemFilterProcessor<Sample4> filterProcessor = new UnmodifiedItemFilterProcessor<>();
		filterProcessor.setChecker(checker);
		filterProcessor.afterPropertiesSet();

		ListenerSupportCompositeItemProcessor compositeProcessor = new ListenerSupportCompositeItemProcessor();
		compositeProcessor.setDelegates(Arrays.asList(
				new LogAndPassItemProcessor<Sample4>(),
				filterProcessor
		));

		compositeProcessor.afterPropertiesSet();

		return compositeProcessor;
	}

	private ItemWriter<Sample4> writer() {
		JdbcBatchItemWriter<Sample4> writer = new JdbcBatchItemWriter<>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
		writer.setSql("INSERT INTO sample4 (id_int, id_str, val_float, val_int, val_str, update_time) " +
				"VALUES (:idInt, :idStr, :valFloat, :valInt, :valStr, :updateTime)");
		writer.setDataSource(dataSource);
		writer.afterPropertiesSet();

		return writer;
	}
}
