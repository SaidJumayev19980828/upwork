--liquibase formatted sql

--changeset MohamedShaker:store_checkouts_table dbms:postgresql splitStatements:true failOnError:true

-- table to temporarily store the employee doing the checkout on behave of a user
CREATE TABLE public.store_checkouts
(
    id bigint,
    employee_id bigint NOT NULL,
    user_id bigint NOT NULL,
    organization_id bigint NOT NULL,
    shop_id bigint NOT NULL
);

ALTER TABLE IF EXISTS public.store_checkouts
    OWNER to nasnav;

CREATE SEQUENCE IF NOT EXISTS public.store_checkouts_id_seq
    INCREMENT 1
    START 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE store_checkouts ALTER COLUMN id SET DEFAULT nextval('store_checkouts_id_seq');
