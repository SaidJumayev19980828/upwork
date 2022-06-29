--liquibase formatted sql

--changeset abdel-salam:add-transaction-start-date dbms:postgresql splitStatements:false failOnError:true

--comment: add start date to loyalty transaction

ALTER TABLE public.loyalty_point_transactions ADD start_date timestamp without time zone NOT NULL;