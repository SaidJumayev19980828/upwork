--liquibase formatted sql

--changeset abdel-salam:add-spend-points-in-order dbms:postgresql splitStatements:false failOnError:true

--comment: add loyalty points spend transactions

create Table loyalty_spent_transactions (
     id bigserial not null Primary Key,
     transaction_id bigint not null References loyalty_point_transactions(id),
     reverse_transaction_id bigint not null References loyalty_point_transactions(id),
     meta_order_id bigint null References meta_orders(id)
);

ALTER TABLE public.loyalty_spent_transactions ADD UNIQUE (transaction_id, reverse_transaction_id);