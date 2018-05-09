package com.naver.spring.batch.extension;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class NaverSpringBatchExtensionTestApplication {
	public static void main(String[] args) {
		SpringApplication.run(NaverSpringBatchExtensionTestApplication.class, args);

	}
}
