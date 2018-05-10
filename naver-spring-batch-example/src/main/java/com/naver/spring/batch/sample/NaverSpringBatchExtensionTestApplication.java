package com.naver.spring.batch.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class NaverSpringBatchExtensionTestApplication {
	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(
				NaverSpringBatchExtensionTestApplication.class, args)));
	}
}
