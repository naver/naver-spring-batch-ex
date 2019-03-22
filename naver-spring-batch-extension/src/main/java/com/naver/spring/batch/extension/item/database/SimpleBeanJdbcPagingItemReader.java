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
package com.naver.spring.batch.extension.item.database;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.*;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * JdbcPagingItemReader 와 동일하게 동작한다.
 * 하지만 {@link PagingQueryProvider} 를 통해 query 의 정보를 넘겨줘야 하는 JdbcPagingItemReader 와 달리
 * 생성시 넘겨받는 mappedClass 정보를 통해 paging query 를 자동으로 생성해 준다.
 * </p>
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public class SimpleBeanJdbcPagingItemReader<T> extends AbstractPagingItemReader<T> implements InitializingBean {
	private static final String START_AFTER_VALUE = "start.after";

	private DataSource dataSource;

	private PagingQueryProvider queryProvider;

	private Map<String, Object> parameterValues;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private RowMapper<T> rowMapper;

	private String firstPageSql;

	private String remainingPagesSql;

	private Map<String, Object> startAfterValues;

	private Map<String, Object> previousStartAfterValues;

	private Class<T> mappedClass;
	private String tableName;
	private String whereClause;
	private Map<String, String> columnMappings;
	private Map<String, Order> sortKeys;
	private List<String> excludePropertyNames;

	public SimpleBeanJdbcPagingItemReader(Class<T> mappedClass) {
		setName(ClassUtils.getShortName(SimpleBeanJdbcPagingItemReader.class));
		this.mappedClass = mappedClass;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 쿼리 실행에 사용될 파라미터 값이다.
	 * 만약 named parameter 를 사용하려면 key 는 쿼리에 포함된 name 이 되어야 하고,
	 * placeholders (?) 를 사용하려면 key 는 relative index 가 되어야 한다.
	 *
	 * @param parameterValues the values keyed by the parameter named/index used
	 * in the query string.
	 */
	public void setParameterValues(Map<String, Object> parameterValues) {
		this.parameterValues = parameterValues;
	}
	/**
	 * from clause 에 들어갈 테이블명. 빈값이면 mappedClass 의 camelcaseToUnderscore 처리된 class 명
	 *
	 * @param tableName tableName (default: mappedClass 의 camelcaseToUnderscore 처리된 class 명)
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	/**
	 * select clause 에 나열될 column 이름은 기본적으로 propertyName 을 underscore 표현으로 처리하지만
	 * 직접 매핑이 필요한 경우 추가해 준다.
	 *
	 * @param columnMappings map of {propertyName : column name}
	 */
	public void setColumnMappings(Map<String, String> columnMappings) {
		this.columnMappings = columnMappings;
	}

	/**
	 * Result 매핑에 사용될 rowMapper. default BeanPropertyRowMapper 가 사용됨
	 * @param rowMapper
	 */
	public void setRowMapper(RowMapper<T> rowMapper) {
		this.rowMapper = rowMapper;
	}

	/**
	 * @param sortKeys the sortKeys to set
	 */
	public void setSortKeys(Map<String, Order> sortKeys) {
		this.sortKeys = sortKeys;
	}

	public void setSortKey(String key) {
		Assert.doesNotContain(key, ",", "String setter is valid for a single ASC key only");

		Map<String, Order> keys = new LinkedHashMap<String, Order>();
		keys.put(key, Order.ASCENDING);

		this.sortKeys = keys;
	}

	/**
	 * query 생성시 제외할 property 이름 목록
	 * @param excludePropertyNames
	 */
	public void setExcludePropertyNames(List<String> excludePropertyNames) {
		this.excludePropertyNames = excludePropertyNames;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();

		Assert.notNull(dataSource, "dataSource is required");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.setMaxRows(getPageSize());
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		List<String> columnNames = new ArrayList<>();

		for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(mappedClass)) {
			if (pd.getWriteMethod() != null) {
				String propName = pd.getName();

				if (excludePropertyNames != null && excludePropertyNames.contains(propName)) {
					continue;
				}

				if (this.columnMappings != null && this.columnMappings.containsKey(propName)) {
					columnNames.add(this.columnMappings.get(propName));
				} else {
					columnNames.add(camelcaseToUnderscore(propName));
				}
			}
		}

		if (this.rowMapper == null) {
			this.rowMapper = new BeanPropertyRowMapper<>(mappedClass);
		}

		if (this.tableName == null) {
			this.tableName = camelcaseToUnderscore(mappedClass.getSimpleName());
		}

		AbstractSqlPagingQueryProvider queryProvider = determineQueryProvider(dataSource);
		queryProvider.setSelectClause(String.join(",", columnNames));
		queryProvider.setFromClause(this.tableName);
		queryProvider.setWhereClause(this.whereClause);
		queryProvider.setSortKeys(this.sortKeys);
		queryProvider.init(dataSource);

		this.queryProvider = queryProvider;

		this.firstPageSql = queryProvider.generateFirstPageQuery(getPageSize());
		this.remainingPagesSql = queryProvider.generateRemainingPagesQuery(getPageSize());
	}

	/*
	 * copied from spring-batch
	 * https://github.com/spring-projects/spring-batch/blob/master/spring-batch-infrastructure/src/main/java/org/springframework/batch/item/database/JdbcPagingItemReader.java
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void doReadPage() {
		if (results == null) {
			results = new CopyOnWriteArrayList<>();
		}
		else {
			results.clear();
		}

		PagingRowMapper rowCallback = new PagingRowMapper();

		List<?> query;

		if (getPage() == 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL used for reading first page: [" + firstPageSql + "]");
			}
			if (parameterValues != null && parameterValues.size() > 0) {
				if (this.queryProvider.isUsingNamedParameters()) {
					query = namedParameterJdbcTemplate.query(firstPageSql,
							getParameterMap(parameterValues, null), rowCallback);
				}
				else {
					query = getJdbcTemplate().query(firstPageSql,
							getParameterList(parameterValues, null).toArray(), rowCallback);
				}
			}
			else {
				query = getJdbcTemplate().query(firstPageSql, rowCallback);
			}

		}
		else {
			previousStartAfterValues = startAfterValues;
			if (logger.isDebugEnabled()) {
				logger.debug("SQL used for reading remaining pages: [" + remainingPagesSql + "]");
			}
			if (this.queryProvider.isUsingNamedParameters()) {
				query = namedParameterJdbcTemplate.query(remainingPagesSql,
						getParameterMap(parameterValues, startAfterValues), rowCallback);
			}
			else {
				query = getJdbcTemplate().query(remainingPagesSql,
						getParameterList(parameterValues, startAfterValues).toArray(), rowCallback);
			}
		}

		Collection<T> result = (Collection<T>) query;
		results.addAll(result);
	}

	/*
	 * copied from spring-batch
	 * https://github.com/spring-projects/spring-batch/blob/master/spring-batch-infrastructure/src/main/java/org/springframework/batch/item/database/JdbcPagingItemReader.java
	 */
	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		super.update(executionContext);
		if (isSaveState()) {
			if (isAtEndOfPage() && startAfterValues != null) {
				// restart on next page
				executionContext.put(getExecutionContextKey(START_AFTER_VALUE), startAfterValues);
			} else if (previousStartAfterValues != null) {
				// restart on current page
				executionContext.put(getExecutionContextKey(START_AFTER_VALUE), previousStartAfterValues);
			}
		}
	}

	/*
	 * copied from spring-batch
	 * https://github.com/spring-projects/spring-batch/blob/master/spring-batch-infrastructure/src/main/java/org/springframework/batch/item/database/JdbcPagingItemReader.java
	 */
	private boolean isAtEndOfPage() {
		return getCurrentItemCount() % getPageSize() == 0;
	}

	/*
	 * copied from spring-batch
	 * https://github.com/spring-projects/spring-batch/blob/master/spring-batch-infrastructure/src/main/java/org/springframework/batch/item/database/JdbcPagingItemReader.java
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void open(ExecutionContext executionContext) {
		if (isSaveState()) {
			startAfterValues = (Map<String, Object>) executionContext.get(getExecutionContextKey(START_AFTER_VALUE));

			if(startAfterValues == null) {
				startAfterValues = new LinkedHashMap<>();
			}
		}

		super.open(executionContext);
	}

	/*
	 * copied from spring-batch
	 * https://github.com/spring-projects/spring-batch/blob/master/spring-batch-infrastructure/src/main/java/org/springframework/batch/item/database/JdbcPagingItemReader.java
	 */
	@Override
	protected void doJumpToPage(int itemIndex) {
		if (startAfterValues == null && getPage() > 0) {

			String jumpToItemSql = queryProvider.generateJumpToItemQuery(itemIndex, getPageSize());

			if (logger.isDebugEnabled()) {
				logger.debug("SQL used for jumping: [" + jumpToItemSql + "]");
			}

			if (this.queryProvider.isUsingNamedParameters()) {
				startAfterValues = namedParameterJdbcTemplate.queryForMap(jumpToItemSql, getParameterMap(parameterValues, null));
			}
			else {
				startAfterValues = getJdbcTemplate().queryForMap(jumpToItemSql, getParameterList(parameterValues, null).toArray());
			}
		}
	}

	/*
	 * copied from spring-batch
	 * https://github.com/spring-projects/spring-batch/blob/master/spring-batch-infrastructure/src/main/java/org/springframework/batch/item/database/JdbcPagingItemReader.java
	 */
	private Map<String, Object> getParameterMap(Map<String, Object> values, Map<String, Object> sortKeyValues) {
		Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();
		if (values != null) {
			parameterMap.putAll(values);
		}
		if (sortKeyValues != null && !sortKeyValues.isEmpty()) {
			for (Map.Entry<String, Object> sortKey : sortKeyValues.entrySet()) {
				parameterMap.put("_" + sortKey.getKey(), sortKey.getValue());
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Using parameterMap:" + parameterMap);
		}
		return parameterMap;
	}

	/*
	 * copied from spring-batch
	 * https://github.com/spring-projects/spring-batch/blob/master/spring-batch-infrastructure/src/main/java/org/springframework/batch/item/database/JdbcPagingItemReader.java
	 */
	private List<Object> getParameterList(Map<String, Object> values, Map<String, Object> sortKeyValue) {
		SortedMap<String, Object> sm = new TreeMap<>();
		if (values != null) {
			sm.putAll(values);
		}
		List<Object> parameterList = new ArrayList<>();
		parameterList.addAll(sm.values());
		if (sortKeyValue != null && sortKeyValue.size() > 0) {
			List<Map.Entry<String, Object>> keys = new ArrayList<>(sortKeyValue.entrySet());

			for(int i = 0; i < keys.size(); i++) {
				for(int j = 0; j < i; j++) {
					parameterList.add(keys.get(j).getValue());
				}

				parameterList.add(keys.get(i).getValue());
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Using parameterList:" + parameterList);
		}
		return parameterList;
	}

	/*
	 * copied from spring-batch
	 * https://github.com/spring-projects/spring-batch/blob/master/spring-batch-infrastructure/src/main/java/org/springframework/batch/item/database/JdbcPagingItemReader.java
	 */
	private class PagingRowMapper implements RowMapper<T> {
		@Override
		public T mapRow(ResultSet rs, int rowNum) throws SQLException {
			startAfterValues = new LinkedHashMap<>();
			for (Map.Entry<String, Order> sortKey : queryProvider.getSortKeys().entrySet()) {
				startAfterValues.put(sortKey.getKey(), rs.getObject(sortKey.getKey()));
			}

			return rowMapper.mapRow(rs, rowNum);
		}
	}

	/*
	 * copied from spring-batch
	 * https://github.com/spring-projects/spring-batch/blob/master/spring-batch-infrastructure/src/main/java/org/springframework/batch/item/database/JdbcPagingItemReader.java
	 */
	private JdbcTemplate getJdbcTemplate() {
		return (JdbcTemplate) namedParameterJdbcTemplate.getJdbcOperations();
	}

	private String camelcaseToUnderscore(final String name) {
		if (!StringUtils.hasLength(name)) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		result.append(name.substring(0, 1).toLowerCase());

		for (int i = 1; i < name.length(); i++) {
			String s = name.substring(i, i + 1);
			String slc = s.toLowerCase();
			if (!s.equals(slc)) {
				result.append("_").append(slc);
			}
			else {
				result.append(s);
			}
		}

		return result.toString();
	}

	/*
	 * copied from spring-batch
	 * https://github.com/spring-projects/spring-batch/blob/547533fab072289c062916e51c589de36ea3dfe2/spring-batch-infrastructure/src/main/java/org/springframework/batch/item/database/builder/JdbcPagingItemReaderBuilder.java
	 */
	private AbstractSqlPagingQueryProvider determineQueryProvider(DataSource dataSource) {
		try {
			DatabaseType databaseType = DatabaseType.fromMetaData(dataSource);
			AbstractSqlPagingQueryProvider provider;

			switch (databaseType) {

				case DERBY: provider = new DerbyPagingQueryProvider(); break;
				case DB2:
				case DB2VSE:
				case DB2ZOS:
				case DB2AS400: provider = new Db2PagingQueryProvider(); break;
				case H2: provider = new H2PagingQueryProvider(); break;
				case HSQL: provider = new HsqlPagingQueryProvider(); break;
				case SQLSERVER: provider = new SqlServerPagingQueryProvider(); break;
				case MYSQL: provider = new MySqlPagingQueryProvider(); break;
				case ORACLE: provider = new OraclePagingQueryProvider(); break;
				case POSTGRES: provider = new PostgresPagingQueryProvider(); break;
				case SYBASE: provider = new SybasePagingQueryProvider(); break;
				case SQLITE: provider = new SqlitePagingQueryProvider(); break;
				default:
					throw new IllegalArgumentException("Unable to determine PagingQueryProvider type " +
							"from database type: " + databaseType);
			}

			return provider;
		} catch (MetaDataAccessException e) {
			throw new IllegalArgumentException("Unable to determine PagingQueryProvider type", e);
		}
	}
}
