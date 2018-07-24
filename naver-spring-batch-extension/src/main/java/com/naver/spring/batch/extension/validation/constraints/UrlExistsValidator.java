package com.naver.spring.batch.extension.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <p>
 * Url 이 실제로 접근 되는지를 HEAD 요청을 통해 확인하여 Url 에 대한 유효성을 검증한다.
 * </p>
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public class UrlExistsValidator implements ConstraintValidator<UrlExists, String> {
	private final String USER_AGENT = "Mozilla/5.0";

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		try {
			URL url = new URL(value);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("HEAD");
			con.setRequestProperty("User-Agent", USER_AGENT);
			int responseCode = con.getResponseCode();

			return responseCode == 200;
		} catch (java.io.IOException ignored) {
		}

		return false;
	}
}
