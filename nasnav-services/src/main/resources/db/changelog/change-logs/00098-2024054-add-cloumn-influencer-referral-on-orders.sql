--liquibase formatted sql

--changeset MohamedShaker:add-column-inlfuencer-code-on-orders-table dbms:postgresql splitStatements:true failOnError:true


ALTER TABLE IF EXISTS public.orders
    ADD COLUMN applied_influencer_referral_code character varying;