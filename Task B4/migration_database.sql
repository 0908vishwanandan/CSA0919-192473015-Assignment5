CREATE DATABASE IF NOT EXISTS assignment_persistence;

USE assignment_persistence;

CREATE TABLE IF NOT EXISTS bus_pass (

    pass_id VARCHAR(20) PRIMARY KEY,

    reg_no VARCHAR(20) NOT NULL,

    name VARCHAR(100) NOT NULL,

    route_no INT NOT NULL,

    amount DECIMAL(10,2) NOT NULL

) ENGINE = InnoDB;
