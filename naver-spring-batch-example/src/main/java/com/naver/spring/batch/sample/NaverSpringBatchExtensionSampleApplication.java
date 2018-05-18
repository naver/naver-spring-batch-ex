package com.naver.spring.batch.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource(value = "classpath:jobs/*.xml")
@SpringBootApplication
public class NaverSpringBatchExtensionSampleApplication {
	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(
				NaverSpringBatchExtensionSampleApplication.class, args)));
	}
}
