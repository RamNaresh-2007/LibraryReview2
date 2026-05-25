-- SQL Script to set up the Digital Library Management database in PostgreSQL
-- Run this in your PostgreSQL console (psql) or via a tool like pgAdmin.

-- 1. Create the database (if not exists)
-- Note: You might need to run this separately as most tools don't allow CREATE DATABASE inside a transaction.
-- CREATE DATABASE library_db;

-- 2. Connect to the database
-- \c library_db;

-- 3. The tables will be automatically created by Spring Boot due to:
-- spring.jpa.hibernate.ddl-auto=update

-- If you want to manually create them or reset them, you can use these (optional):
/*
DROP TABLE IF EXISTS loans CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    fullname VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    status VARCHAR(50),
    joined_date VARCHAR(50),
    loans INTEGER DEFAULT 0
);

CREATE TABLE books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(50),
    category VARCHAR(100),
    available BOOLEAN DEFAULT TRUE
);

CREATE TABLE loans (
    id SERIAL PRIMARY KEY,
    member_name VARCHAR(255) NOT NULL,
    member_role VARCHAR(50) NOT NULL,
    book_title VARCHAR(255) NOT NULL,
    isbn VARCHAR(50),
    issued_date VARCHAR(50) NOT NULL,
    due_date VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'active'
);
*/
