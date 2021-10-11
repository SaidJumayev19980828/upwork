--liquibase formatted sql

--changeset bassam:add-loyalty-points-tables-missing-tables dbms:postgresql splitStatements:false failOnError:true

--comment: add  tables for loyalty points (booster, charity, coins_drop, coins_drop_logs, family, gift, tier, user_charity)


CREATE TABLE public.booster (
	booster_name varchar NOT NULL,
	linked_family_member int4 NOT NULL,
	number_family_children int4 NOT NULL,
	review_products int4 NULL,
	number_purchase_offline int4 NULL,
	social_media_reviews int4 NULL,
	organization_id int8 NULL,
	level_booster int4 NULL,
	activation_months int4 NULL,
	id bigserial NOT NULL,
	is_active bool NULL,
	purchase_size int4 NULL,
	CONSTRAINT booster_pk PRIMARY KEY (id),
	CONSTRAINT booster_org_fk FOREIGN KEY (organization_id) REFERENCES public.organizations(id)
);



CREATE TABLE public.charity (
	id bigserial NOT NULL,
	charity_name varchar NOT NULL,
	total_donation int4 NULL,
	is_active bool NULL,
	created_at timestamp NULL,
	organization_id int8 NULL,
	CONSTRAINT charity_pk PRIMARY KEY (id),
	CONSTRAINT charity_org_fk FOREIGN KEY (organization_id) REFERENCES public.organizations(id)
);

CREATE TABLE public.coins_drop (
	id bigserial NOT NULL,
	organization_id int8 NULL,
	type_id int4 NULL,
	created_at timestamp NULL,
	amount int4 NULL,
	official_vacation_date date NULL,
	is_active bool NULL,
	CONSTRAINT coins_drop_pk PRIMARY KEY (id),
	CONSTRAINT coins_drop_org_fk FOREIGN KEY (organization_id) REFERENCES public.organizations(id)
);



CREATE TABLE public.coins_drop_logs (
	id bigserial NOT NULL,
	organization_id int8 NULL,
	coins_drop_id int8 NULL,
	created_by int8 NULL,
	is_active bool NULL,
	CONSTRAINT coins_drop_logs_pk PRIMARY KEY (id),
	CONSTRAINT coins_drop_logs_coins_drop_fk FOREIGN KEY (coins_drop_id) REFERENCES public.coins_drop(id),
	CONSTRAINT coins_drop_logs_org_fk FOREIGN KEY (organization_id) REFERENCES public.organizations(id),
	CONSTRAINT coins_drop_logs_user_fk FOREIGN KEY (created_by) REFERENCES public.users(id)
);



CREATE TABLE public."family" (
	id bigserial NOT NULL,
	family_name varchar NULL,
	parent_id int8 NULL,
	booster_id int8 NULL,
	is_active bool NULL,
	created_at timestamp NULL,
	organization_id int8 NULL,
	CONSTRAINT family_pk PRIMARY KEY (id),
	CONSTRAINT family__org_fk FOREIGN KEY (organization_id) REFERENCES public.organizations(id),
	CONSTRAINT family_user_fk FOREIGN KEY (parent_id) REFERENCES public.users(id)
);

CREATE TABLE public.gift (
	id bigserial NOT NULL,
	user_from_id bigint NOT NULL,
	user_to_id bigint NOT NULL,
	points int4 NULL,
	email varchar NULL,
	phone_number varchar NULL,
	is_active boolean NULL,
	is_redeem boolean NULL,
	CONSTRAINT gift_pk PRIMARY KEY (id),
	CONSTRAINT gift_user_from_fk FOREIGN KEY (user_from_id) REFERENCES public.users(id),
	CONSTRAINT gift_user_to_fk FOREIGN KEY (user_to_id) REFERENCES public.users(id)
);

CREATE TABLE public.tier (
	id bigserial NOT NULL,
	tier_name varchar NULL,
	is_active bool NOT NULL DEFAULT true,
	is_special bool NOT NULL DEFAULT false,
	created_at timestamp NULL,
	no_of_purchase_from int4 NULL,
	no_of_purchase_to int4 NULL,
	selling_price int4 NULL,
	organization_id int8 NULL,
	booster_id int8 NULL,
	CONSTRAINT tier_pk PRIMARY KEY (id),
	CONSTRAINT tier_organization_id_fk FOREIGN KEY (organization_id) REFERENCES public.organizations(id)
);


CREATE TABLE public.user_charity (
	id bigserial NOT NULL,
	donation_percentage int4 NULL,
	is_active bool NOT NULL DEFAULT true,
	created_at timestamp NULL,
	charity_id int8 NULL,
	user_id int8 NULL,
	CONSTRAINT user_charity_pk PRIMARY KEY (id),
	CONSTRAINT user_charity_charity_id_fk FOREIGN KEY (charity_id) REFERENCES public.user_charity(id),
	CONSTRAINT user_charity_user_id_fk FOREIGN KEY (user_id) REFERENCES public.users(id)
);




ALTER TABLE public.loyalty_point_transactions ADD got_online boolean NULL;
ALTER TABLE public.loyalty_point_transactions ADD charity_id bigint NULL;
ALTER TABLE public.loyalty_point_transactions ADD is_donate boolean NULL;
ALTER TABLE public.loyalty_point_transactions ADD gift_id bigint NULL;
ALTER TABLE public.loyalty_point_transactions ADD is_gift boolean NULL;
ALTER TABLE public.loyalty_point_transactions ADD coins_drop_id bigint NULL;
ALTER TABLE public.loyalty_point_transactions ADD is_coins_drop boolean NULL;
ALTER TABLE public.loyalty_point_transactions ADD CONSTRAINT loyalty_point_transactions_charity_id_fk FOREIGN KEY (charity_id) REFERENCES public.charity(id);
ALTER TABLE public.loyalty_point_transactions ADD CONSTRAINT loyalty_point_transactions_gift_id_fk FOREIGN KEY (gift_id) REFERENCES public.gift(id);
ALTER TABLE public.loyalty_point_transactions ADD CONSTRAINT loyalty_point_transactions_coins_drop_id_fk FOREIGN KEY (coins_drop_id) REFERENCES public.coins_drop(id);

ALTER TABLE public.tags ADD allow_reward boolean NOT NULL DEFAULT false;
ALTER TABLE public.tags ADD buy_with_coins boolean NULL;
ALTER TABLE public.tags ADD only_buy_with_coins boolean NULL;
ALTER TABLE public.tags ADD minimum_tier_id bigint NULL;


ALTER TABLE public.products ADD allow_reward boolean NOT NULL DEFAULT false;
ALTER TABLE public.products ADD buy_with_coins boolean NULL;
ALTER TABLE public.products ADD only_buy_with_coins boolean NULL;
ALTER TABLE public.products ADD minimum_tier_id bigint NULL;

ALTER TABLE public.promotions ADD priority int4 NULL;
ALTER TABLE public.shops ADD priority int4 NULL;


ALTER TABLE public.users ADD allow_reward boolean NOT NULL DEFAULT true;
ALTER TABLE public.users ADD tier_created_at timestamp NULL;
ALTER TABLE public.users ADD booster_id bigint NULL;
ALTER TABLE public.users ADD booster_created timestamp NULL;
ALTER TABLE public.users ADD date_of_birth timestamp NULL;
ALTER TABLE public.users ADD CONSTRAINT users_booster_id_fk FOREIGN KEY (booster_id) REFERENCES public.booster(id);


ALTER TABLE public.users ADD family_id bigint NULL;
ALTER TABLE public.users ADD tier_id bigint NULL;
ALTER TABLE public.users ADD CONSTRAINT users_tier_id_fk FOREIGN KEY (tier_id) REFERENCES public.tier(id);





