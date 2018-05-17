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