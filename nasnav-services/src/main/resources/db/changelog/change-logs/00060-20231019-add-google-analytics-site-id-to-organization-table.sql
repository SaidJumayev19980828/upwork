--liquibase formatted sql

--changeset Ihab:add_organization_google_analytics_column dbms:postgresql splitStatements:false failOnError:true

--comment: add_organization_google_analytics_column

ALTER TABLE public.organizations ADD COLUMN google_analytics_site_id text NOT NULL DEFAULT '';