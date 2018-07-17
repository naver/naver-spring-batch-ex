package com.naver.spring.batch.sample.job.unmodified_filter;

import com.naver.spring.batch.extension.item.ListenerSupportCompositeItemProcessor;
import com.naver.spring.batch.extension.item.filter.HashUnmodifiedItemChecker;
import com.naver.spring.batch.extension.item.filter.JdbcHashRepository;
import com.naver.spring.batch.extension.item.filter.UnmodifiedItemFilterProcessor;
import com.naver.spring.batch.sample.config.LoggingItemWriter;
import com.naver.spring.batch.sample.domain.Sample4;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
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
				.reader(new Sample4ItemReader())
				.processor(processor())
				.writer(new LoggingItemWriter<>())
				.build();
	}

	private ItemProcessor<Sample4, Sample4> processor() throws Exception {
		HashUnmodifiedItemChecker<Sample4> checker = new HashUnmodifiedItemChecker<>();
		checker.setHashRepository(new JdbcHashRepository(dataSource, transactionManager));
		checker.setKeyPropertyNames(Arrays.asList("idInt", "idStr"));
		checker.afterPropertiesSet();

		UnmodifiedItemFilterProcessor<Sample4> filterProcessor = new UnmodifiedItemFilterProcessor<>();
		filterProcessor.setChecker(checker);
		filterProcessor.afterPropertiesSet();

		LogAndPassItemProcessor<Sample4> logAndPassItemProcessor = new LogAndPassItemProcessor<>();

		ListenerSupportCompositeItemProcessor<Sample4, Sample4> compositeProcessor = new ListenerSupportCompositeItemProcessor<>();
		compositeProcessor.setDelegates(Arrays.asList(
				logAndPassItemProcessor,
				filterProcessor
		));

		compositeProcessor.afterPropertiesSet();

		return compositeProcessor;
	}
}
