--liquibase formatted sql

--changeset abdel-salam:update_shop_id_nullable_in_loyalty_trans dbms:postgresql splitStatements:false failOnError:true

--comment: make shop_id not required in yeshtery points transactions

ALTER TABLE public.loyalty_point_transactions ALTER COLUMN shop_id DROP NOT NULL;