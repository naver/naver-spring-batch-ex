package com.naver.spring.batch.extension.item.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Restful api 로 부터 데이터를 읽어들이는 경우 Reader 구조를 제공한다.
 * 여러개의 variables 로부터 요청하는 경우 {@link AsyncListenableTaskExecutor} 를 통해 Async 요청을 수행한다.
 * response 에 대해서 {@link #convertResponse(ResponseEntity, Map)} ()} 를 통해 Object 매핑 방식을 하위 클래스에서 구현한다.
 * </p>
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public abstract class AbstractAsyncRestItemReader<T> implements ItemReader<T>, InitializingBean {
	private Logger log = LoggerFactory.getLogger(AbstractAsyncRestItemReader.class);

	private AsyncRestTemplate asyncRestTemplate;
	private Map<Map<String, ?>, ListenableFuture<ResponseEntity<String>>> responseMap = null;
	private Iterator<T> itemIterator = null;
	private Iterator<Map<String, ?>> uriVariablesIterator;

	private String apiUri;

	private List<Map<String, ?>> uriVariables;

	private AsyncListenableTaskExecutor asyncListenableTaskExecutor;

	private HttpHeaders headers;

	private int responseTimeout = 30;

	public void setApiUri(String apiUri) {
		this.apiUri = apiUri;
	}

	public void setUriVariables(List<Map<String, ?>> uriVariables) {
		this.uriVariables = uriVariables;
	}

	public void setAsyncListenableTaskExecutor(AsyncListenableTaskExecutor asyncListenableTaskExecutor) {
		this.asyncListenableTaskExecutor = asyncListenableTaskExecutor;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public void setResponseTimeout(int responseTimeout) {
		this.responseTimeout = responseTimeout;
	}

	/**
	 * responseEntiry 로부터 response body 를 parsing 해 List 로 리턴하도록 구현한다.
	 *
	 * @param responseEntity null 이면 responseEntity 반환에 실패.
	 * @param uriVariable uri 에 포함된 변수 목록
	 * @return response 의 List 형태
	 */
	abstract protected List<T> convertResponse(ResponseEntity<String> responseEntity, Map<String, ?> uriVariable);

	@Override
	public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		requestAsyncRestApis();

		if (itemIterator == null || !itemIterator.hasNext()) {

			while (this.uriVariablesIterator.hasNext()) {
				Map<String, ?> uriVariable = this.uriVariablesIterator.next();
				ListenableFuture<ResponseEntity<String>> listenableFuture = responseMap.get(uriVariable);
				ResponseEntity<String> responseEntity = listenableFuture.get(this.responseTimeout, TimeUnit.SECONDS);

				List<T> results = convertResponse(responseEntity, uriVariable);

				if (results != null && !results.isEmpty()) {
					this.itemIterator = results.iterator();
					break;
				}
			}
		}

		return (itemIterator != null && itemIterator.hasNext()) ? itemIterator.next() : null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(apiUri, "'apiUri' is required");

		if (asyncListenableTaskExecutor != null) {
			asyncRestTemplate = new AsyncRestTemplate(asyncListenableTaskExecutor);
		} else {
			asyncRestTemplate = new AsyncRestTemplate();
		}
		asyncRestTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
	}

	private void requestAsyncRestApis() {
		if (this.responseMap != null) {
			return;
		}

		if (this.uriVariables == null || this.uriVariables.isEmpty()) {
			this.uriVariables = Collections.singletonList(Collections.emptyMap());
		}

		this.uriVariablesIterator = this.uriVariables.iterator();
		this.responseMap = new HashMap<>();

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(apiUri);

		if (this.headers == null) {
			this.headers = new HttpHeaders();
		}

		for (Map<String, ?> uriVariable : this.uriVariables) {
			URI uri = uriBuilder.buildAndExpand(uriVariable).toUri();

			if (log.isDebugEnabled()) {
				log.debug("request uri: {}", uri.toString());
			}

			ListenableFuture<ResponseEntity<String>> resFuture =
					asyncRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(this.headers), String.class);

			this.responseMap.put(uriVariable, resFuture);
		}
	}
}