-- Run this once to create the database user (optional, if using root skip this)
CREATE DATABASE IF NOT EXISTS nit_audit_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- If you want a dedicated user instead of root:
-- CREATE USER 'nitaudit'@'localhost' IDENTIFIED BY 'nitaudit123';
-- GRANT ALL PRIVILEGES ON nit_audit_db.* TO 'nitaudit'@'localhost';
-- FLUSH PRIVILEGES;

-- Tables are auto-created by Spring Boot (ddl-auto=update)
-- Benchmark seed data is inserted automatically on first startup
