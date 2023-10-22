--liquibase formatted sql

--changeset Hussien:rocket_chat_organization_departments dbms:postgresql splitStatements:false failOnError:true

--comment: add rocket_chat_organization_departments table

CREATE TABLE public.rocket_chat_organization_departments(
    org_id bigint not null Primary Key References public.organizations(id),
    department_id varchar not null
);