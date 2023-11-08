--liquibase formatted sql

--changeset Assem:alter service table dbms:postgresql splitStatements:false failOnError:true

--comment: add light_logo , dark_logo , enabled columns to service table

ALTER TABLE public.service
    ADD COLUMN IF NOT EXISTS light_logo  VARCHAR(255),
    ADD COLUMN IF NOT EXISTS dark_logo   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS enabled     BOOLEAN default true NOT NULL;

