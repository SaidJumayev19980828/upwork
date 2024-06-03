--liquibase formatted sql

--changeset MohamedShaker:influencer_referral dbms:postgresql splitStatements:true failOnError:true

CREATE SEQUENCE IF NOT EXISTS public.store_checkouts_id_seq
    INCREMENT 1
    START 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE influencer_referral (
        id bigint PRIMARY KEY DEFAULT nextval('store_checkouts_id_seq'),
        first_name VARCHAR(255),
        last_name VARCHAR(255),
        user_name VARCHAR(255) UNIQUE,
        password VARCHAR(255),
        referral_code_id bigint,
        referral_settings_id bigint,
        referral_wallet_id bigint
);

ALTER TABLE IF EXISTS public.referral_settings DROP CONSTRAINT IF EXISTS referral_settings_org_id_key;

ALTER TABLE IF EXISTS public.referral_wallet DROP CONSTRAINT IF EXISTS fk_user_id;

ALTER TABLE IF EXISTS public.referral_transactions DROP CONSTRAINT IF EXISTS fk_user_id;