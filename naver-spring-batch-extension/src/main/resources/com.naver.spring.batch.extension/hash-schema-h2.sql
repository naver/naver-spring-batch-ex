CREATE TABLE BATCHEX_ITEM_HASH (
  `item_key` varchar(200) NOT NULL PRIMARY KEY,
  `item_hash` varchar(50) NOT NULL,
  `expiry` datetime NOT NULL
);