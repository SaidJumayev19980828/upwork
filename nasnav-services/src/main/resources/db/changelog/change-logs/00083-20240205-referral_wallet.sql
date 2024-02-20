-- liquibase formatted sql
--changeset MohamedShaker:referral_wallet dbms:postgresql splitStatements:false failOnError:true

--comment: referral_wallet table
CREATE TABLE IF NOT EXISTS public.referral_wallet
(
    id bigint NOT NULL,
    balance numeric NOT NULL DEFAULT 0.0,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    version integer,
    user_id bigint NOT NULL,
    CONSTRAINT referral_wallet_pkey PRIMARY KEY (id),
    CONSTRAINT referral_wallet_user_id_key UNIQUE (user_id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id)
    REFERENCES public.users (id) MATCH SIMPLE
                         ON UPDATE NO ACTION
                         ON DELETE CASCADE
);
ALTER TABLE public.referral_wallet OWNER TO nasnav;

CREATE SEQUENCE IF NOT EXISTS public.referral_wallet_id_seq
    INCREMENT 1
    START 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.referral_wallet_id_seq OWNER TO nasnav;
ALTER TABLE referral_wallet ALTER COLUMN id SET DEFAULT nextval('referral_wallet_id_seq');


CREATE TABLE IF NOT EXISTS public.referral_settings
(
    id bigint NOT NULL,
    org_id bigint NOT NULL,
    constraints text COLLATE pg_catalog."default" NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT "ReferralSettings_pkey" PRIMARY KEY (id)
);
ALTER TABLE public.referral_settings OWNER TO nasnav;

CREATE SEQUENCE IF NOT EXISTS public.referral_settings_id_seq
    INCREMENT 1
    START 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE public.referral_settings_id_seq OWNER TO nasnav;
ALTER TABLE referral_settings ALTER COLUMN id SET DEFAULT nextval('referral_settings_id_seq');



CREATE TABLE IF NOT EXISTS public.referral_codes
(
    id bigint NOT NULL,
    referral_code character varying(6) COLLATE pg_catalog."default" NOT NULL,
    parent_referral_code character varying(6) COLLATE pg_catalog."default" NOT NULL,
    org_id bigint NOT NULL,
    user_id bigint NOT NULL,
    constraints text COLLATE pg_catalog."default" NOT NULL,
    settings_id bigint NOT NULL,
    created_by bigint NOT NULL,
    status integer NOT NULL DEFAULT 0,
    created_at timestamp without time zone DEFAULT now(),
    accept_token text COLLATE pg_catalog."default",
    CONSTRAINT referal_codes_pkey PRIMARY KEY (id),
    CONSTRAINT unique_referal_code UNIQUE (referral_code),
    CONSTRAINT fk_settings_id FOREIGN KEY (settings_id)
    REFERENCES public.referral_settings (id) MATCH SIMPLE
                         ON UPDATE NO ACTION
                         ON DELETE NO ACTION
                         NOT VALID
    );
ALTER TABLE public.referral_codes OWNER TO nasnav;

CREATE SEQUENCE IF NOT EXISTS public.referral_codes_id_seq
    INCREMENT 1
    START 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE public.referral_codes_id_seq OWNER TO nasnav;
ALTER TABLE referral_codes ALTER COLUMN id SET DEFAULT nextval('referral_codes_id_seq');


CREATE TABLE IF NOT EXISTS public.referral_transactions
(
    id bigint NOT NULL,
    amount numeric NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    referral_transaction_type character varying(255) COLLATE pg_catalog."default" NOT NULL,
    referral_wallet_id bigint,
    user_id bigint NOT NULL,
    order_id bigint,
    referral_id bigint NOT NULL,
    CONSTRAINT referral_transactions_pkey PRIMARY KEY (id),
    CONSTRAINT fk_referral_id FOREIGN KEY (referral_id)
    REFERENCES public.referral_codes (id) MATCH SIMPLE
                         ON UPDATE NO ACTION
                         ON DELETE CASCADE,
    CONSTRAINT fk_referral_wallet_id FOREIGN KEY (referral_wallet_id)
    REFERENCES public.referral_wallet (id) MATCH SIMPLE
                         ON UPDATE NO ACTION
                         ON DELETE CASCADE,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id)
    REFERENCES public.users (id) MATCH SIMPLE
                         ON UPDATE NO ACTION
                         ON DELETE CASCADE
);
ALTER TABLE public.referral_transactions OWNER TO nasnav;

CREATE SEQUENCE IF NOT EXISTS public.referral_transactions_id_seq
    INCREMENT 1
    START 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER TABLE public.referral_transactions_id_seq OWNER TO nasnav;
ALTER TABLE referral_transactions ALTER COLUMN id SET DEFAULT nextval('referral_transactions_id_seq');


-- ALTER TABLE orders ADD COLUMN applied_referral_code character varying COLLATE pg_catalog."default";

