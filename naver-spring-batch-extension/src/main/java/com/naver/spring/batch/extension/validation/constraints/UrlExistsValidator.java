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
package com.naver.spring.batch.extension.validation.constraints;

import java.net.HttpURLConnection;
import java.net.URL;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * <p>
 * Url 이 실제로 접근 되는지를 HEAD 요청을 통해 확인하여 Url 에 대한 유효성을 검증한다.
 * </p>
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public class UrlExistsValidator implements ConstraintValidator<UrlExists, String> {
	private static final String USER_AGENT = "Mozilla/5.0";

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		int responseCode = -1;
		HttpURLConnection con = null;

		try {
			URL url = new URL(value);

			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("HEAD");
			con.setRequestProperty("User-Agent", USER_AGENT);
			responseCode = con.getResponseCode();

		} catch (java.io.IOException ignored) {
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		return responseCode == 200;

	}
}