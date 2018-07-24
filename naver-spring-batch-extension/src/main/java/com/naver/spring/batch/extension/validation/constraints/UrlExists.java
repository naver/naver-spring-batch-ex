package com.naver.spring.batch.extension.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 이 annotation 이 붙은 url 은 접근이 가능해야 한다.
 *
 * @author yongkyu.lee
 * @since 0.1
 */
@Documented
@Constraint(validatedBy = UrlExistsValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlExists {
	String message() default "Url does not exist";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
