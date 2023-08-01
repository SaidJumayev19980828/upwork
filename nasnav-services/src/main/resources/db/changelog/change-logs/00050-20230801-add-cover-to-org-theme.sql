--liquibase formatted sql

--changeset Hussien:add_cover_to_org_theme dbms:postgresql splitStatements:false failOnError:true

--comment: add cover to org theme

ALTER TABLE public.organization_themes ADD COLUMN IF NOT EXISTS cover character varying NULL;
