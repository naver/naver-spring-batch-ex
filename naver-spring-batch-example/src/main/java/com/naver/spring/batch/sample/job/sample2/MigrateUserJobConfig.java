package com.naver.spring.batch.sample.job.sample2;

import com.naver.spring.batch.sample.job.sample.Person;
import com.naver.spring.batch.sample.job.sample.PersonItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MigrateUserJobConfig {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;

	@Autowired
	public MigrateUserJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		this.dataSource = dataSource;
	}

	@Bean
	public Job migrateUserJob() throws Exception {
		return jobBuilderFactory.get("migrateUserJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
	}

	private Step step1() throws Exception {
		return stepBuilderFactory.get("step1")
				.<Person, Person> chunk(10)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}

	// tag::readerwriterprocessor[]

	private JdbcPagingItemReader<Person> reader() throws Exception {
		SqlPagingQueryProviderFactoryBean providerFactoryBean = new SqlPagingQueryProviderFactoryBean();
		providerFactoryBean.setDataSource(dataSource);
		providerFactoryBean.setSelectClause("select *");
		providerFactoryBean.setFromClause("from people");
		providerFactoryBean.setSortKey("person_id");

		JdbcPagingItemReader<Person> reader = new JdbcPagingItemReader<>();
		reader.setDataSource(dataSource);
		reader.setQueryProvider(providerFactoryBean.getObject());

		reader.setRowMapper((rs, i) -> new Person(rs.getString("last_name"), rs.getString("first_name")));
		reader.afterPropertiesSet();

		return reader;
	}

	private PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}

	private JdbcBatchItemWriter<Person> writer() {
		JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
		writer.setSql("INSERT INTO people2 (first_name, last_name) VALUES (:firstName, :lastName)");
		writer.setDataSource(dataSource);
		writer.afterPropertiesSet();

		return writer;
	}
	// end::readerwriterprocessor[]

}
