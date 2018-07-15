CREATE USER softwareberg IDENTIFIED BY 'softwareberg';

CREATE DATABASE softwareberg CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON softwareberg.* TO softwareberg;
