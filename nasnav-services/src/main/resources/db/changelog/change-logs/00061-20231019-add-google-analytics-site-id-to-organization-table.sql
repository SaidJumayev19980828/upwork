--liquibase formatted sql

--changeset Ihab:drop_and_add_organization_google_analytics_column dbms:postgresql splitStatements:false failOnError:true

--comment: drop_and_add_organization_google_analytics_column

ALTER TABLE public.organizations
DROP COLUMN google_analytics_site_id;

ALTER TABLE public.organizations ADD COLUMN google_analytics_site_id text DEFAULT '';