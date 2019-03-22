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

/**
 * 변경되지 않는 아이템인지 체크함.
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public interface UnmodifiedItemChecker<T> {
	/**
	 * item에 대해 변경 여부를 판단한다.
	 *
	 * @param item 변경 여부를 확인할 item
	 * @return true - 변경되지 않음, false - 변경되었거나 처음 처리됨
	 */
	boolean check(T item) throws Exception;
}
