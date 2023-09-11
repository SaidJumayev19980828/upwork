--liquibase formatted sql

--changeset Doaa:add-spcial-order-to-cart dbms:postgresql splitStatements:false failOnError:true

--comment: add-spcial-order-to-cart


ALTER TABLE public.cart_items ADD COLUMN IF NOT EXISTS  special_order text;

ALTER TABLE public.baskets ADD COLUMN IF NOT EXISTS  special_order text;
