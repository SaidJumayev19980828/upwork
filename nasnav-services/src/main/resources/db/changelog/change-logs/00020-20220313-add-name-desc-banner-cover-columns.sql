--liquibase formatted sql

--changeset AbdelSalam:add_name-desc-banner-cover-columns dbms:postgresql splitStatements:false failOnError:true

--comment: add_name-desc-banner-cover-columns

ALTER TABLE public.promotions ADD COLUMN name text;
ALTER TABLE public.promotions ADD COLUMN description text;
ALTER TABLE public.promotions ADD COLUMN cover text;
ALTER TABLE public.promotions ADD COLUMN banner text;