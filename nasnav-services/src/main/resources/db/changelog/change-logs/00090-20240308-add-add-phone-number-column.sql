--liquibase formatted sql

--changeset MohamedShaker:organizations dbms:postgresql splitStatements:false failOnError:true

--comment: ADD PHONE as text

ALTER TABLE IF EXISTS public.referral_codes ADD COLUMN phone_number character varying COLLATE pg_catalog."default";

