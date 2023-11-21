--liquibase formatted sql

--changeset Moamen:room_templates dbms:postgresql splitStatements:false failOnError:true

--comment: drop not null constraint from room_templates table

ALTER TABLE room_templates
ALTER COLUMN data DROP NOT NULL;