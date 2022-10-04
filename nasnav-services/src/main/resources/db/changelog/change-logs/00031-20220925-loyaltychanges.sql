--liquibase formatted sql

--changeset ahmad-abdelsalam:loyalty-changes dbms:postgresql splitStatements:false failOnError:true

--comment: remove unnecessary columns, add constraints columns

delete from loyalty_spent_transactions;
delete from loyalty_point_transactions;
delete from loyalty_point_config;
update users set tier_id = null;
delete from loyalty_tier;

ALTER TABLE public.loyalty_tier ADD constraints text null;
ALTER TABLE public.loyalty_tier drop COLUMN coefficient;

ALTER TABLE public.loyalty_point_config ADD constraints text null;
ALTER TABLE public.loyalty_point_config drop COLUMN ratio_from;
ALTER TABLE public.loyalty_point_config drop COLUMN ratio_to;
ALTER TABLE public.loyalty_point_config drop COLUMN coefficient;
ALTER TABLE public.loyalty_point_config drop COLUMN expiry;

ALTER TABLE public.loyalty_point_transactions ADD type integer not null;
ALTER TABLE public.loyalty_point_transactions drop COLUMN loyalty_point_id;
ALTER TABLE public.loyalty_point_transactions drop COLUMN got_online;
ALTER TABLE public.loyalty_point_transactions drop COLUMN is_donate;
ALTER TABLE public.loyalty_point_transactions drop COLUMN is_gift;


ALTER TABLE public.loyalty_pins ALTER COLUMN user_id DROP NOT NULL;