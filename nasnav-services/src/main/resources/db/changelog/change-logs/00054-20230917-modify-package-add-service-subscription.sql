--liquibase formatted sql

--changeset Mark:modify-package-create-service-subscription dbms:postgresql splitStatements:false failOnError:true

--comment: add period to package

ALTER TABLE public.package ADD COLUMN period bigint NULL ;

--comment: create service

CREATE SEQUENCE IF NOT EXISTS public.service_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.service_seq
    OWNER TO nasnav;

CREATE TABLE IF NOT EXISTS public.service
(
    id bigint NOT NULL DEFAULT nextval('service_seq'::regclass),
    code text unique,
    name text,
    description text,
    CONSTRAINT service_pkey PRIMARY KEY (id)
);

--
-- Name: package_service; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.package_service (
                                     package_id bigint NOT NULL,
                                     service_id bigint NOT NULL
);

ALTER TABLE public.package_service OWNER TO nasnav;


