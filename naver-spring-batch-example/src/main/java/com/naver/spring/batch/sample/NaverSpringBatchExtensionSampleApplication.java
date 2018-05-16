package com.naver.spring.batch.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NaverSpringBatchExtensionSampleApplication {
	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(
				NaverSpringBatchExtensionSampleApplication.class, args)));
	}
}
