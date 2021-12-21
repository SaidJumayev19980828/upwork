--liquibase formatted sql

--changeset Bassam:change_lo_pts_col_types dbms:postgresql splitStatements:false failOnError:true

--comment:change loyalty points columns type and add coefficient in tier

ALTER TABLE public.loyalty_point_transactions ALTER COLUMN points TYPE numeric USING points::numeric;
ALTER TABLE public.loyalty_gift ALTER COLUMN points TYPE numeric USING points::numeric;

ALTER TABLE public.loyalty_points ALTER COLUMN amount TYPE numeric USING amount::numeric;
ALTER TABLE public.loyalty_points ALTER COLUMN points TYPE numeric USING points::numeric;

ALTER TABLE public.loyalty_coins_drop ALTER COLUMN amount TYPE numeric USING amount::numeric;

ALTER TABLE public.loyalty_tier ADD coefficient numeric NULL;

INSERT INTO public.loyalty_tier
(id, tier_name, is_active, is_special, created_at, no_of_purchase_from, no_of_purchase_to, selling_price, organization_id, booster_id, cash_back_percentage, coefficient)
VALUES(1, 'Default tier', true, false, now() , NULL, NULL, NULL, NULL, NULL, 0, 0.0);


update loyalty_point_config set default_tier_id = 1 where default_tier_id is null;
