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

import com.naver.spring.batch.extension.item.filter.HashIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class Sample4 {
	private int idInt;
	private String idStr;
	private Float valFloat;
	private Integer valInt;
	private String valStr;
	@HashIgnore
	private Date updateTime;

	public static Sample4 of(int idInt, String idStr, Float valFloat, Integer valInt, String valStr, Date updateTime) {
		Sample4 sample4 = new Sample4();
		sample4.setIdInt(idInt);
		sample4.setIdStr(idStr);
		sample4.setValFloat(valFloat);
		sample4.setValInt(valInt);
		sample4.setValStr(valStr);
		sample4.setUpdateTime(updateTime);

		return sample4;
	}
}
