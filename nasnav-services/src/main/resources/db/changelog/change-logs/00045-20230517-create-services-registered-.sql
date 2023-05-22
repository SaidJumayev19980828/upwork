--liquibase formatted sql

--changeset  Eslam:create services dbms:postgresql splitStatements:false failOnError:true

--comment: create services and services_registered

CREATE TABLE public.services (
                                 id bigint NOT NULL,
                                            service_name text,
                                                service_cost numeric(10,2) DEFAULT 0
);

--comment: create services_registered

CREATE TABLE public.services_registered (
                                            id bigint NOT NULL,
                                            user_id bigint NOT NULL,
                                            service_id bigint NOT NULL,
                                            registered_date timestamp without time zone NOT NULL
);

