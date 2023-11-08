--liquibase formatted sql

--changeset Alaa:add showing-online column dbms:postgresql splitStatements:false failOnError:true

--comment: add showing online

ALTER TABLE public.promotions ADD COLUMN showing_online boolean DEFAULT false NOT NULL ;
