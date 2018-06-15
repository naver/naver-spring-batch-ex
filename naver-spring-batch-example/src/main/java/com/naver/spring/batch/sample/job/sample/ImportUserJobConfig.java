package com.naver.spring.batch.sample.job.sample;

import com.naver.spring.batch.extension.item.ListenerSupportCompositeItemProcessor;
import com.naver.spring.batch.extension.item.filter.UnmodifiedItemFilterProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class ImportUserJobConfig {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;

	@Autowired
	public ImportUserJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		this.dataSource = dataSource;
	}

	// tag::jobstep[]
	@Bean
	public Job importUserJob() throws Exception {
		return jobBuilderFactory.get("importUserJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
	}

	private Step step1() throws Exception {
		return stepBuilderFactory.get("step1")
				.<Person, Person> chunk(3)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}
	// end::jobstep[]

	// tag::readerwriterprocessor[]
	private FlatFileItemReader<Person> reader() throws Exception {
		FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
		reader.setResource(new ClassPathResource("sample-data.csv"));
		reader.setLineMapper(new DefaultLineMapper<Person>() {{
			setLineTokenizer(new DelimitedLineTokenizer() {{
				setNames(new String[] { "firstName", "lastName" });
			}});
			setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
				setTargetType(Person.class);
			}});
		}});
		reader.afterPropertiesSet();
		return reader;
	}

	private ItemProcessor<Person, Person> processor() throws Exception {
		ListenerSupportCompositeItemProcessor<Person, Person> p = new ListenerSupportCompositeItemProcessor<>();
		p.setDelegates(Arrays.asList(
				new PersonItemProcessor(),
				new UnmodifiedItemFilterProcessor<>()
		));
		p.afterPropertiesSet();
		return p;
//		return new UnmodifiedItemFilterProcessor<>();
	}

	private JdbcBatchItemWriter<Person> writer() {
		JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
		writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
		writer.setDataSource(dataSource);
		writer.afterPropertiesSet();
		return writer;
	}
	// end::readerwriterprocessor[]
}