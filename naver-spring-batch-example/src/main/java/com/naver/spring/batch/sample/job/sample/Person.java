package com.naver.spring.batch.sample.job.sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Person {
	private String lastName;
	private String firstName;
}
