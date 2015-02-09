DROP DATABASE IF EXISTS alliance_db;
CREATE DATABASE IF NOT EXISTS alliance_db;

USE alliance_db;

CREATE TABLE entities1
(
	id 			bigint 			NOT NULL PRIMARY KEY AUTO_INCREMENT,
	name 		varchar(32) 	NOT NULL,
	friendID	bigint			NOT NULL
);

CREATE TABLE secure1
(
	id 			bigint 			NOT NULL PRIMARY KEY,
	passwordHash varchar(255) 	NOT NULL
);

CREATE TABLE loginKeys1
(
	userID 		bigint 			NOT NULL PRIMARY KEY,
	userKey 	varchar(64) 	NOT NULL
);

CREATE TABLE tableamounts
(
	tableName 	varchar(32) 	NOT NULL PRIMARY KEY,
	latestIndex int 			NOT NULL
);