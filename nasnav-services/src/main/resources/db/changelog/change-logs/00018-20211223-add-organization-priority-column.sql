--liquibase formatted sql

--changeset AbdelSalam:add_organization_priority_columns dbms:postgresql splitStatements:false failOnError:true

--comment: add_organization_priority_columns

ALTER TABLE public.organizations ADD COLUMN priority integer NOT NULL DEFAULT 0;