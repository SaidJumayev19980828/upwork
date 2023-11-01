--liquibase formatted sql

--changeset Moamen:advertisements dbms:postgresql splitStatements:false failOnError:true

--comment: add name column to advertisements

alter table public.advertisements
    add column if not exists name text
