package com.naver.spring.batch.extension.item.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hash값 저장시 key 로 사용될 property 를 지정하기 위해 사용된다. ({@link HashUnmodifiedItemChecker})
 *
 * @author yongkyu.lee
 * @since 0.1
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface HashKey {
}
