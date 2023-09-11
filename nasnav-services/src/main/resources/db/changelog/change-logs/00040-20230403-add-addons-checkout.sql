--liquibase formatted sql

--changeset Doaa:add-addons-basket dbms:postgresql splitStatements:false failOnError:true

--comment: add-addons-basket



CREATE TABLE IF NOT EXISTS public.addon_basket
(
     id  SERIAL PRIMARY KEY NOT NULL,
    basket_id bigint,
    addon_stock_id bigint,
    price numeric(10,2) DEFAULT 0
)
