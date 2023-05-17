--liquibase formatted sql

--changeset Hussien Assem:add payment_confirmations dbms:postgresql splitStatements:false failOnError:true

--comment: add payment_confirmations to organization_payments

ALTER TABLE IF EXISTS public.organization_payments
    ADD COLUMN IF NOT EXISTS payment_confirmations boolean;
