--liquibase formatted sql

--changeset abdelsalam:add-loyalty-points-tables dbms:postgresql splitStatements:false failOnError:true

--comment: add 3 tables for loyalty points (loyalty_points, loyalty_point_types, loyalty_point_transactions)


Create Table loyalty_point_types (
     id bigserial not null Primary Key,
     name text null
);

Create Table loyalty_point_config (
    id bigserial not null Primary Key,
    description text null,
    organization_id bigint not null References Organizations(id),
    shop_id bigint not null References shops(id),
    amount_from Integer,
    amount_to Integer,
    points Integer,
    is_active boolean default true,
    created_at timestamp without time zone NOT NULL
);

Create Table loyalty_points (
    id bigserial not null Primary Key,
    description text null,
    organization_id bigint not null References Organizations(id),
    type_id bigint not null,
    amount Integer,
    points Integer,
    start_date timestamp without time zone NOT NULL,
    end_date timestamp without time zone NOT NULL
);

Create Table loyalty_point_transactions (
    id bigserial not null Primary Key,
    user_id bigint not null References Users(id),
    shop_id bigint not null References shops(id),
    loyalty_point_id bigint null References loyalty_points(id),
    order_id bigint null References orders(id),
    created_at timestamp without time zone NOT NULL,
    points Integer,
    is_valid boolean default true
);

ALTER TABLE public.shops ADD COLUMN code text;

ALTER TABLE public.shops ADD COLUMN allow_other_points boolean;