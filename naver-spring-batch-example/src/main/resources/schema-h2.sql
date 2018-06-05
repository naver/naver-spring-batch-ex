CREATE TABLE IF NOT EXISTS people (
    person_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS people2 (
    person_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS people3 (
    seq int AUTO_INCREMENT PRIMARY KEY NOT NULL,
    name varchar(20),
    age int,
    email varchar(50),
    birth_day datetime,
    phone_no varchar(20),
    homepage_url varchar(1000)
);

CREATE TABLE IF NOT EXISTS sample4
(
    seq int AUTO_INCREMENT PRIMARY KEY NOT NULL,
    id_int int NOT NULL,
    id_str varchar(50) NOT NULL,
    val_float decimal(5,2),
    val_int int,
    val_str varchar(50),
    update_time datetime NOT NULL,
);
CREATE INDEX IF NOT EXISTS sample4_id_int_id_str_index ON sample4 (id_int, id_str);



CREATE TABLE IF NOT EXISTS BATCHEX_ITEM_HASH (
    item_key varchar(100) PRIMARY KEY NOT NULL,
    item_hash varchar(50) NOT NULL,
    expiry datetime NOT NULL
);