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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Item 에 대한 hash값 생성시 제외할 property 를 표시하는데 사용된다. ({@link HashUnmodifiedItemChecker})
 *
 * timestamp 와 같이 매번 처리될 때마다 변경되는 property 가 있다면 이 Annotation 을 통해 hash source 에서 제외하도록 할 수 있다.
 *
 * @author yongkyu.lee
 * @since 0.1
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface HashIgnore {
}
