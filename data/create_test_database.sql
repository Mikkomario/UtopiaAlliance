DROP DATABASE IF EXISTS alliance_db;
CREATE DATABASE IF NOT EXISTS alliance_db;

USE alliance_db;

CREATE TABLE test1
(
	id 			bigint 			NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name 		varchar(32) 	NOT NULL,
	friendID	bigint			NOT NULL
);

CREATE TABLE tableamounts
(
	tableName 	varchar(32) 	NOT NULL PRIMARY KEY,
	latestIndex int 			NOT NULL
);