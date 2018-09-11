/*
Copyright 2018 NAVER Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.naver.spring.batch.sample.domain;

import com.naver.spring.batch.extension.validation.constraints.UrlExists;
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

	@UrlExists
	private String homepageUrl;
}
