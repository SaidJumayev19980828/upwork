--liquibase formatted sql

--changeset MohamedShaker:organizations dbms:postgresql splitStatements:false failOnError:true

--comment: ADD name and constraint only as text

ALTER TABLE IF EXISTS public.referral_settings
    ADD CONSTRAINT referral_settings_org_id_key UNIQUE (org_id);

ALTER TABLE IF EXISTS public.referral_settings
    ADD COLUMN name character varying NOT NULL DEFAULT '';


