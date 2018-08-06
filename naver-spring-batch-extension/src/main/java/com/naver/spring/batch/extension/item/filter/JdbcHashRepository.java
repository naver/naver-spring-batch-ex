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

/**
 * Jdbc 를 통해 hash 값을 저장하는 HashRepository 구현체
 * 지원하는 DB: H2, MySql
 *
 * @author yongkyu.lee
 * @since 0.1
 */
public class JdbcHashRepository implements HashRepository {
	private static final Logger log = LoggerFactory.getLogger(JdbcHashRepository.class);

	private static final String selectSql = "SELECT item_hash FROM BATCHEX_ITEM_HASH WHERE item_key = ? AND expiry > ?";
	private static final String saveSqlForH2 = "MERGE INTO BATCHEX_ITEM_HASH (item_key, item_hash, expiry) KEY (item_key) VALUES (?, ?, ?)";
	private static final String saveSqlForMysql = "INSERT INTO BATCHEX_ITEM_HASH (item_key, item_hash, expiry) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE item_hash = VALUES(item_hash), expiry = VALUES(expiry)";
	private static final String deleteExpiredSql = "DELETE FROM BATCHEX_ITEM_HASH WHERE expiry < ? LIMIT 100";

	private final JdbcOperations jdbcOperations;
	private final TransactionOperations transactionOperations;
	private final DatabaseType databaseType;

	/**
	 *
	 * @param dataSource {@link DataSource}
	 * @param platformTransactionManager {@link PlatformTransactionManager}
	 * @throws MetaDataAccessException {@link UnsupportedDatabaseException}
	 */
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
				jdbcOperations.update(deleteExpiredSql, new Date());
			}
		});
	}

	private String getSaveSql() {
		if (this.databaseType == DatabaseType.H2) {
			return saveSqlForH2;
		} else {
			return saveSqlForMysql;
		}
	}
}
