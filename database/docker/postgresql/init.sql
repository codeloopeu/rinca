CREATE USER softwareberg WITH PASSWORD 'softwareberg';
ALTER USER softwareberg WITH SUPERUSER;

CREATE DATABASE softwareberg;

GRANT ALL PRIVILEGES ON DATABASE softwareberg TO softwareberg;
