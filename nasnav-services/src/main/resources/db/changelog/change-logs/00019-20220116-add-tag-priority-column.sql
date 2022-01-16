--liquibase formatted sql

--changeset AbdelSalam:add_tag_priority_columns dbms:postgresql splitStatements:false failOnError:true

--comment: add_tag_priority_columns

ALTER TABLE public.tags ADD COLUMN priority integer NOT NULL DEFAULT 0;