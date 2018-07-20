CREATE TABLE BATCHEX_ITEM_HASH (
  `seq` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_key` varchar(200) NOT NULL,
  `item_hash` varchar(50) NOT NULL,
  `expiry` datetime NOT NULL,
  PRIMARY KEY (`seq`),
  UNIQUE KEY `item_key_UNIQUE` (`item_key`),
  KEY `expiry_IDX` (`expiry`)
) ENGINE=InnoDB;