--liquibase formatted sql

--changeset Hussien:yeshtery_user_otp_table dbms:postgresql splitStatements:false failOnError:true

--comment: create yeshtery user otp table

CREATE SEQUENCE IF NOT EXISTS public.yeshtery_user_otp_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.yeshtery_user_otp_seq
    OWNER TO nasnav;

CREATE TABLE IF NOT EXISTS public.yeshtery_user_otp
(
    otp character varying COLLATE pg_catalog."default" NOT NULL,
    id bigint NOT NULL DEFAULT nextval('user_otp_seq'::regclass),
    user_id bigint NOT NULL References yeshtery_users(id),
    created_at timestamp without time zone NOT NULL,
    type character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT yeshtery_user_otp_pkey PRIMARY KEY (id),
    attempts BIGINT NOT NULL DEFAULT 0
)