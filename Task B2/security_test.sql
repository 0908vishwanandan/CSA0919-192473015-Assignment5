CREATE DATABASE IF NOT EXISTS assignment_security;

USE assignment_security;

CREATE TABLE IF NOT EXISTS users (

    id VARCHAR(50) PRIMARY KEY,

    pwd VARCHAR(100) NOT NULL
);

INSERT INTO users (id, pwd)
VALUES ('admin', 'admin123')
ON DUPLICATE KEY UPDATE pwd = 'admin123';

-- Verify the test account
SELECT * FROM users;
