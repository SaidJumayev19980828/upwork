--liquibase formatted sql

--changeset bassam:add-yeshtery_user_id_refrence_in_user_table dbms:postgresql splitStatements:false failOnError:true

--comment: add yeshtery user id foriegen key in user table

alter table public.users add column yeshtery_user_id BIGINT;