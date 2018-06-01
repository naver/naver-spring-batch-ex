package com.naver.spring.batch.extension.item.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcHashRepository implements HashRepository {
	private static final Logger log = LoggerFactory.getLogger(JdbcHashRepository.class);
	private static final String selectSql = "SELECT item_hash FROM BATCHEX_ITEM_HASH WHERE item_key = ? AND expiry > ?";
	private static final String saveSqlH2 = "MERGE INTO BATCHEX_ITEM_HASH (item_key, item_hash, expiry) KEY (item_key) VALUES (?, ?, ?)";
	private static final String saveSqlMysql = "REPLACE INTO BATCHEX_ITEM_HASH (item_key, item_hash, expiry) VALUES (?, ?, ?)";

	private final JdbcOperations jdbcOperations;
	private final TransactionOperations transactionOperations;
	private final DatabaseType databaseType;

	public JdbcHashRepository(DataSource dataSource, PlatformTransactionManager platformTransactionManager)
			throws MetaDataAccessException {
		this.transactionOperations = new TransactionTemplate(platformTransactionManager);
		this.jdbcOperations = new JdbcTemplate(dataSource);
		this.databaseType = DatabaseType.fromMetaData(dataSource);

		if (this.databaseType != DatabaseType.MYSQL && this.databaseType != DatabaseType.H2) {
			throw new UnsupportedDatabaseException("'" + this.databaseType + "' is not support");
		}
	}

	@Override
	public String getHashValue(String itemKey) {
		try {
			return jdbcOperations.queryForObject(selectSql, String.class, itemKey, new Date());
		} catch (EmptyResultDataAccessException ignored) {}

		return null;
	}

	@Override
	public void saveItemHashes(List<ItemHash> itemHashes) {
		final String sql = getSaveSql();

		final List<Object[]> args = itemHashes.stream()
				.map(p -> new Object[] { p.getItemKey(), p.getItemHash(), p.getExpiry() })
				.collect(Collectors.toList());

		if (log.isDebugEnabled()) {
			log.debug("Save ItemHashes {}", itemHashes.size());
		}

		transactionOperations.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				jdbcOperations.batchUpdate(sql, args);
			}
		});
	}

	private String getSaveSql() {
		if (this.databaseType == DatabaseType.H2) {
			return saveSqlH2;
		} else {
			return saveSqlMysql;
		}
	}
}
