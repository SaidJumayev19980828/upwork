--liquibase formatted sql

--changeset Moamen:event-access_code dbms:postgresql splitStatements:true failOnError:true

ALTER TABLE events ADD COLUMN IF NOT EXISTS access_code VARCHAR(255) UNIQUE ;

ALTER TABLE personal_event ADD COLUMN IF NOT EXISTS status int;

