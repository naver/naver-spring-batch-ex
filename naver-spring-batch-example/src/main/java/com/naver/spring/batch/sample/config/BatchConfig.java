package com.naver.spring.batch.sample.config;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.sql.DataSource;

@ImportResource(value = "classpath:com/naver/spring/batch/sample/job/sample3_validation/BeanValidationJobConfig.xml")
@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {

	//배치 처리정보를 DB에 처리하지 않고 메모리에서 처리하도록
	@Override
	public void setDataSource(DataSource dataSource) {
	}
}
