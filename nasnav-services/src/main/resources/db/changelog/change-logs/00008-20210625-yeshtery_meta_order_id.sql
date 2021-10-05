--liquibase formatted sql

--changeset bassam:add-yeshtery_meta_order_id dbms:postgresql splitStatements:false failOnError:true

--comment: add field yeshtery meta_order_id
ALTER TABLE public.meta_orders ADD yeshtery_meta_order_id int8 NULL;
