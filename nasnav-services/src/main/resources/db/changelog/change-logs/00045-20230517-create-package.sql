--liquibase formatted sql

--changeset Eslam:create package dbms:postgresql splitStatements:false failOnError:true

--comment: create package


CREATE TABLE public.package (
                                      id bigint NOT NULL,
                                      name text,
                                      description text,
                                      price numeric(10,2) DEFAULT 0
);

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_pkey PRIMARY KEY (id);

ALTER TABLE public.package OWNER TO nasnav;

--
-- Name: package_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.package_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.package_id_seq OWNER TO nasnav;
--
-- Name: package id; Type: DEFAULT; Schema: public; Owner: nasnav
--
ALTER TABLE ONLY public.package ALTER COLUMN id SET DEFAULT nextval('public.package_id_seq'::regclass);

--comment: create package_registered
CREATE TABLE public.package_registered (
                                           id bigint NOT NULL,
                                           user_id bigint NOT NULL,
                                           package_id bigint NOT NULL,
                                           registered_date timestamp without time zone NOT NULL
);

ALTER TABLE public.package_registered ADD CONSTRAINT package_registered_user_fk FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.package_registered
    ADD CONSTRAINT package_registered_pkey PRIMARY KEY (id);

ALTER TABLE public.package_registered OWNER TO nasnav;
--
-- Name: package_registered_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--
CREATE SEQUENCE public.package_registered_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.package_registered_id_seq OWNER TO nasnav;
--
-- Name: package_registered id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.package_registered ALTER COLUMN id SET DEFAULT nextval('public.package_registered_id_seq'::regclass);


--comment: create services_registered_in_package
CREATE TABLE public.services_registered_in_package (
                                                       id bigint NOT NULL,
                                                       services_num bigint NOT NULL,
                                                       package_id bigint NOT NULL
);

ALTER TABLE ONLY public.services_registered_in_package
    ADD CONSTRAINT services_registered_in_package_pkey PRIMARY KEY (id);


ALTER TABLE public.services_registered_in_package OWNER TO nasnav;
--
-- Name: services_registered_in_package_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--
CREATE SEQUENCE public.services_registered_in_package_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.services_registered_in_package_id_seq OWNER TO nasnav;
--
-- Name: services_registered_in_package id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.services_registered_in_package ALTER COLUMN id SET DEFAULT nextval('public.services_registered_in_package_id_seq'::regclass);
