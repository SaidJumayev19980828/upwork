--liquibase formatted sql

--changeset abdel-salam:add_expiry-to-loyalty-config-and-transaction dbms:postgresql splitStatements:false failOnError:true

--comment: add expiry value in days to loyalty config and expiry date to loyalty transaction

ALTER TABLE public.loyalty_point_config ADD expiry integer NULL;

ALTER TABLE public.loyalty_point_transactions ADD end_date timestamp without time zone NULL;
ALTER TABLE public.loyalty_point_transactions ADD meta_order_id bigint null References meta_orders(id);