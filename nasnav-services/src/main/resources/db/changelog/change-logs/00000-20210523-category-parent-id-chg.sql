--liquibase formatted sql

--changeset galal:change-category-parent-type dbms:postgresql splitStatements:false failOnError:true

--comment: category parent Id was set to Integer, it should be bigint to match the id type
ALTER TABLE public.categories
ALTER COLUMN parent_id TYPE bigint;