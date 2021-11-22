--liquibase formatted sql

--changeset Bassam:Loyalty_special_events dbms:postgresql splitStatements:false failOnError:true

--comment: Loyalty_special_events

CREATE SEQUENCE public.loyalty_event_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

CREATE TABLE public.loyalty_events (
       id int8 NOT NULL DEFAULT nextval('loyalty_event_id_seq'::regclass),
       "name" varchar not null ,
       organization_id int8 not null,
       start_date timestamp without time zone,
       end_date timestamp without time zone ,
       is_active bool default true not null,
       CONSTRAINT loyalty_events_pk PRIMARY KEY (id),
       CONSTRAINT loyalty_events_fk FOREIGN KEY (organization_id) REFERENCES public.organizations(id)
);
