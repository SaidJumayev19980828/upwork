--liquibase formatted sql

--changeset Moamen:organizations dbms:postgresql splitStatements:false failOnError:true

--comment: add opening_hours and short_description columns to organizations

alter table public.organizations
    add column if not exists opening_hours character varying;

alter table public.organizations
    add column if not exists short_description character varying;