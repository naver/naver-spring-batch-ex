package com.naver.spring.batch.sample.domain;

import com.naver.spring.batch.extension.item.filter.HashIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class Sample4 {
	private int seq;
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
