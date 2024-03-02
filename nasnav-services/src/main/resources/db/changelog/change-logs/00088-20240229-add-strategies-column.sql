--liquibase formatted sql

--changeset Moamen:organizations dbms:postgresql splitStatements:false failOnError:true

--comment: ADD strategies column as text

ALTER TABLE organizations
ADD COLUMN strategies text;

