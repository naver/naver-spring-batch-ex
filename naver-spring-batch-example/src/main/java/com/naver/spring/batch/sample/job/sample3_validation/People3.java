package com.naver.spring.batch.sample.job.sample3_validation;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Positive;
import java.util.Date;

@AllArgsConstructor(staticName = "of")
@Data
public class People3 {
	private int seq;
	private String name;

	@Positive
	private int age;

	@Email
	private String email;
	private Date birthDay;
	private String phoneNo;
	private String homepageUrl;
}
