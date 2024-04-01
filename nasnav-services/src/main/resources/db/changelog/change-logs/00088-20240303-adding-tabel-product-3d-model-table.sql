-- -- liquibase formatted sql
-- --changeset khaled hussein:add new table called product_3d_model
--
-- --comment: adding  product_3d_model table

create table if not exists product_3d_model(
    id bigint primary key ,
    name varchar,
    description TEXT,
    model varchar,
    size bigint,
    barcode varchar,
    sku varchar,
    color varchar
);

ALTER TABLE public.product_3d_model DROP CONSTRAINT IF EXISTS barcode_unique ;
ALTER TABLE public.product_3d_model ADD CONSTRAINT barcode_unique UNIQUE (barcode);

ALTER TABLE public.product_3d_model DROP CONSTRAINT IF EXISTS sku_unique ;
ALTER TABLE public.product_3d_model ADD CONSTRAINT sku_unique UNIQUE (sku);