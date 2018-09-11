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
package com.naver.spring.batch.extension.item.filter;

public class TestObj {
	@HashKey
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
