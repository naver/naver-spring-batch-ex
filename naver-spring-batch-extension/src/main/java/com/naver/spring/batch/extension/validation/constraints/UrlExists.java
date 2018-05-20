package com.naver.spring.batch.extension.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UrlExistsValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlExists {
	String message() default "Url does not exist";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
