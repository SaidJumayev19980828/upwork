--liquibase formatted sql

--changeset MohamedShaker:organizations dbms:postgresql splitStatements:false failOnError:true

--comment: ADD  referral_withdraw_amount as text

ALTER TABLE IF EXISTS public.orders ADD COLUMN referral_withdraw_amount numeric DEFAULT 0.0;

