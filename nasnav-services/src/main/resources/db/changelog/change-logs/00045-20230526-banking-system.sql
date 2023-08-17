--liquibase formatted sql

--changeset mohamed ismail:banking-system-table dbms:postgresql splitStatements:false failOnError:true

--comment: create banking system tables


CREATE TABLE public.bank_accounts(
                             id bigserial not null Primary Key,
                             user_id bigint References users(id),
                             org_id bigint References organizations(id),
                             wallet_address text not null default '',
                             opening_balance float not null default 0,
                             opening_balance_date timestamp without time zone NOT null default now(),
                             locked BOOLEAN not null default false,
                             created_at timestamp without time zone NOT null default now()
);

CREATE TABLE public.bank_inside_transactions(
                             id bigserial not null Primary Key,
                             sender_account_id bigint not null References bank_accounts(id),
                             receiver_account_id bigint not null References bank_accounts(id),
                             amount float not null default 0,
                             activity_date timestamp without time zone NOT null default now()
);

CREATE TABLE public.bank_outside_transactions(
                             id bigserial not null Primary Key,
                             account_id bigint not null References bank_accounts(id),
                             amount_in float not null default 0,
                             amount_out float not null default 0,
                             activity_date timestamp without time zone NOT null default now(),
                             bc_key text
);

CREATE TABLE public.bank_account_activities(
                             id bigserial not null Primary Key,
                             account_id bigint not null References bank_accounts(id),
                             amount_in float not null default 0,
                             amount_out float not null default 0,
                             activity_date timestamp without time zone NOT null default now(),
                             inside_transaction_id bigint References bank_inside_transactions(id),
                             outside_transaction_id bigint  References bank_outside_transactions(id)
);

CREATE TABLE public.bank_reservations(
                             id bigserial not null Primary Key,
                             account_id bigint not null References bank_accounts(id),
                             amount float not null default 0,
                             activity_date timestamp without time zone NOT null default now(),
                             fulfilled BOOLEAN not null default false,
                             fulfilled_date timestamp without time zone default now()
);

ALTER TABLE public.bank_accounts
    ADD COLUMN opening_balance_activity_id bigint REFERENCES bank_account_activities(id);
