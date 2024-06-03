--liquibase formatted sql

--changeset MohamedShaker:influencer_referral dbms:postgresql splitStatements:true failOnError:true

ALTER TABLE IF EXISTS public.referral_transactions
    ADD COLUMN referral_type character varying NOT NULL DEFAULT 'USER';

ALTER TABLE IF EXISTS public.referral_codes
    ADD COLUMN referral_type character varying NOT NULL DEFAULT 'USER';

ALTER TABLE IF EXISTS public.referral_settings
    ADD COLUMN referral_type character varying NOT NULL DEFAULT 'USER';

ALTER TABLE IF EXISTS public.referral_wallet
    ADD COLUMN referral_type character varying NOT NULL DEFAULT 'USER';

