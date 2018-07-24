package com.naver.spring.batch.extension.item.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * {@link HashUnmodifiedItemChecker} 가 Item 에 대한 hash 값 생성시
 * @HashIgnore 로 표시한 property 혹은 getter method 에 대해서는 hash input 에서 제외 한다.
 * </p>
 *
 * @author yongkyu.lee
 * @since 0.1
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface HashIgnore {
}
