--liquibase formatted sql

--changeset Bassam:rename_loyalty_tables_and_set_default_for_boolean_columns dbms:postgresql splitStatements:false failOnError:true

--comment: rename loyalty tables and set default for boolean columns


ALTER TABLE public.booster RENAME TO loyalty_booster;
ALTER TABLE public.charity RENAME TO loyalty_charity;
ALTER TABLE public.coins_drop RENAME TO loyalty_coins_drop;
ALTER TABLE public.coins_drop_logs RENAME TO loyalty_coins_drop_logs;
ALTER TABLE public."family" RENAME TO loyalty_family;
ALTER TABLE public.gift RENAME TO loyalty_gift;
ALTER TABLE public.tier RENAME TO loyalty_tier;


ALTER TABLE public.loyalty_booster ALTER COLUMN is_active SET DEFAULT true;
ALTER TABLE public.loyalty_charity ALTER COLUMN is_active SET DEFAULT true;
ALTER TABLE public.loyalty_coins_drop ALTER COLUMN is_active SET DEFAULT true;
ALTER TABLE public.loyalty_coins_drop ALTER COLUMN is_active SET DEFAULT true;
ALTER TABLE public.loyalty_coins_drop_logs ALTER COLUMN is_active SET DEFAULT true;
ALTER TABLE public.loyalty_family ALTER COLUMN is_active SET DEFAULT true;
ALTER TABLE public.loyalty_gift ALTER COLUMN is_active SET DEFAULT true;
ALTER TABLE public.loyalty_gift ALTER COLUMN is_redeem SET DEFAULT false;
ALTER TABLE public.loyalty_point_transactions ALTER COLUMN is_valid SET DEFAULT true;
ALTER TABLE public.loyalty_point_transactions ALTER COLUMN got_online SET DEFAULT false;
ALTER TABLE public.loyalty_point_transactions ALTER COLUMN is_donate SET DEFAULT false;
ALTER TABLE public.loyalty_point_transactions ALTER COLUMN is_gift SET DEFAULT false;
ALTER TABLE public.loyalty_point_transactions ALTER COLUMN is_coins_drop SET DEFAULT false;
ALTER TABLE public.loyalty_tier ALTER COLUMN is_special SET DEFAULT false;
ALTER TABLE public.loyalty_tier ALTER COLUMN is_active SET DEFAULT true;
ALTER TABLE public.loyalty_coins_drop ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE public.loyalty_family ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE public.loyalty_point_config ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE public.loyalty_point_transactions ALTER COLUMN created_at SET DEFAULT now();
ALTER TABLE public.loyalty_tier ALTER COLUMN created_at SET DEFAULT now();


ALTER TABLE  public.loyalty_coins_drop ADD  CONSTRAINT coins_drop_type_fk FOREIGN KEY (type_id) REFERENCES public.loyalty_point_types(id);
ALTER TABLE  public.loyalty_coins_drop_logs ADD  CONSTRAINT coins_drop_log_drop_fk FOREIGN KEY (coins_drop_id) REFERENCES public.loyalty_coins_drop(id);
ALTER TABLE  public.loyalty_family ADD  CONSTRAINT family_booster_fk FOREIGN KEY (booster_id) REFERENCES public.loyalty_booster(id);
ALTER TABLE  public.loyalty_points ADD  CONSTRAINT loyalty_points_type_fk FOREIGN KEY (type_id) REFERENCES public.loyalty_point_types(id);

ALTER TABLE  public.users ADD  CONSTRAINT user_family_fk FOREIGN KEY (family_id) REFERENCES public.loyalty_family(id);


ALTER TABLE public.loyalty_point_config ADD default_tier_id int8 REFERENCES loyalty_tier(id);
ALTER TABLE public.loyalty_point_transactions ADD org_id int8 REFERENCES organizations(id);

ALTER TABLE public.loyalty_point_config DROP COLUMN amount_from;
ALTER TABLE public.loyalty_point_config DROP COLUMN amount_to;
ALTER TABLE public.loyalty_point_config DROP COLUMN points;

ALTER TABLE public.loyalty_tier ADD cash_back_percentage numeric;
