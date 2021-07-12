--liquibase formatted sql

--changeset abdelsalam:add-social-link-whatsapp dbms:postgresql splitStatements:false failOnError:true

--comment: add field for whatsapp link to be used by frontend team
ALTER TABLE public.social_links ADD COLUMN whatsapp text;