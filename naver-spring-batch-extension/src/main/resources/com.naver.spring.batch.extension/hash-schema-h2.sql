CREATE TABLE BATCHEX_ITEM_HASH (
    item_key varchar(100) PRIMARY KEY NOT NULL,
    item_hash varchar(50) NOT NULL,
    expiry datetime NOT NULL
);