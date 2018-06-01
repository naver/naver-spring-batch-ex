CREATE TABLE BATCHEX_ITEM_HASH (
  item_key varchar(100) NOT NULL,
  item_hash varchar(50) NOT NULL,
  expiry datetime NOT NULL,
  PRIMARY KEY (item_key)
) ENGINE=InnoDB;