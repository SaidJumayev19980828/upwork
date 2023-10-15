--liquibase formatted sql

--changeset Mark:modify-package-create-service-subscription dbms:postgresql splitStatements:false failOnError:true

--comment: add period to package

ALTER TABLE public.package ADD COLUMN period_in_days bigint;
ALTER TABLE public.package ADD COLUMN currency_iso Integer;
ALTER TABLE public.package ADD CONSTRAINT package_currency_iso_fk FOREIGN KEY (currency_iso) REFERENCES public.countries(iso_code);

--comment: add constraint to package_registered

ALTER TABLE public.package_registered ADD CONSTRAINT package_registered_package_id_fk FOREIGN KEY (package_id) REFERENCES public.package(id);


--comment: create service table

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

--comment: Delete services_registered_in_package table

Delete FROM public.services_registered_in_package;

--comment: create subscription table

CREATE SEQUENCE IF NOT EXISTS public.subscription_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.subscription_seq
    OWNER TO nasnav;

CREATE TABLE IF NOT EXISTS public.subscription
(
    id bigint NOT NULL DEFAULT nextval('subscription_seq'::regclass),
    type text,
    payment_date timestamp,
    start_date timestamp without time zone,
    expiration_date timestamp without time zone,
    paid_amount numeric(10,2) DEFAULT 0,
    package_id BIGINT,
    org_id BIGINT,
    status text NOT NULL,
    CONSTRAINT subscription_pkey PRIMARY KEY (id),
    CONSTRAINT subscription_package_package_id_fkey FOREIGN KEY (package_id) REFERENCES public.package(id),
    CONSTRAINT subscription_organizations_organizations_id_fkey FOREIGN KEY (org_id) REFERENCES public.organizations(id) ON DELETE CASCADE
);
--comment: Add Owner To Organization Table
ALTER TABLE public.organizations ADD column owner_id BIGINT NULL;
ALTER TABLE public.organizations ADD CONSTRAINT organizations_owner_id_fk FOREIGN KEY (owner_id) REFERENCES public.employee_users(id) ON DELETE SET NULL;


