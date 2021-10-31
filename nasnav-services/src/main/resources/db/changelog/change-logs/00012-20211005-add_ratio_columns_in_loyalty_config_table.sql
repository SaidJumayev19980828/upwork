--liquibase formatted sql

--changeset Bassam:add_ratio_columns_in_loyalty_config_table dbms:postgresql splitStatements:false failOnError:true

--comment: add Ratios Columns in Loyalty Config Table

alter table public.loyalty_point_config add column ratio_from numeric;
alter table public.loyalty_point_config add column ratio_to numeric;
alter table public.loyalty_point_config add column coefficient numeric;
ALTER TABLE public.loyalty_point_config ALTER COLUMN shop_id DROP NOT NULL;
