--liquibase formatted sql

--changeset mohamed ismail:create--call-queue-table dbms:postgresql splitStatements:false failOnError:true

--comment: create call queue table for logs of calls

CREATE TABLE public.call_queue(
                                      id bigserial not null Primary Key,
                                      user_id bigint not null References users(id),
                                      employee_id bigint References employee_users(id),
                                      organization_id bigint not null References Organizations(id),
                                      joins_at timestamp without time zone NOT null default now(),
                                      starts_at timestamp without time zone,
                                      ends_at timestamp without time zone,
                                      reason text,
                                      status integer not null default (0)
);