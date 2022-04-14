--liquibase formatted sql

--changeset bassam:add_paymob_source_identifier dbms:postgresql splitStatements:false failOnError:true

--comment: add_paymob_source_identifier

ALTER TABLE public.paymob_source ADD identifier varchar NOT NULL;