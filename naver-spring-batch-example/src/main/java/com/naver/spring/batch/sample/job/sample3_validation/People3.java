package com.naver.spring.batch.sample.job.sample3_validation;

import lombok.Data;

import java.util.Date;

@Data
public class People3 {
	private int seq;
	private String name;
	private int age;
	private String email;
	private Date birthDay;
	private String phoneNo;
	private String homepageUrl;
}
