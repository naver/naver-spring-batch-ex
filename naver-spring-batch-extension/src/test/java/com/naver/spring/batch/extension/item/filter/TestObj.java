package com.naver.spring.batch.extension.item.filter;

public class TestObj {
	private int id;
	private String name;
	private int age;

	private int randomVal;
	@HashIgnore
	private int randomVal2;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getRandomVal() {
		return randomVal;
	}

	public void setRandomVal(int randomVal) {
		this.randomVal = randomVal;
	}

	public int getRandomVal2() {
		return randomVal2;
	}

	public void setRandomVal2(int randomVal2) {
		this.randomVal2 = randomVal2;
	}
}