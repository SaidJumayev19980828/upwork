--liquibase formatted sql

--changeset sameh:rename_amount_column_in_orders_table_to_subtotal dbms:postgresql splitStatements:false failOnError:true

--comment: rename amount column in orders table

ALTER TABLE public.orders RENAME COLUMN amount TO sub_total;