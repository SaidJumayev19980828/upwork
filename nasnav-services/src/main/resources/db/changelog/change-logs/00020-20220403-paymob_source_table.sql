--liquibase formatted sql

--changeset bassam:add_paymob_source_table dbms:postgresql splitStatements:false failOnError:true

--comment: add_paymob_source_table
create Table paymob_source (
           id bigserial not null Primary Key,
           created_at timestamp without time zone NOT null default now(),
           value varchar(50) not null,
           name text not null,
           type varchar(50) not null,
           status varchar(50) not null,
           currency varchar(3) not null,
           script text not null,
           icon text null,
           organization_id bigint not null References Organizations(id)

);