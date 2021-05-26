--liquibase formatted sql

--changeset abdelsalam:add-user-cart-item-date dbms:postgresql splitStatements:false failOnError:true

--comment: add user cart item date to save cart update time
ALTER TABLE public.cart_items ADD COLUMN created_at timestamp without time zone;