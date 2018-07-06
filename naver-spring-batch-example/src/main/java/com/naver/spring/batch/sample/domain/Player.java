package com.naver.spring.batch.sample.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Player {
	private long id;
	private String playerName;
	private int age;
	private Integer height;
	private int ranking;
}