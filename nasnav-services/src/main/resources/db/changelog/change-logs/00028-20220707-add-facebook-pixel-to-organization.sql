--liquibase formatted sql

--changeset abdel-salam:add-facebook-pixel-to-organization dbms:postgresql splitStatements:false failOnError:true

--comment: add facebook pixel id per organization

ALTER TABLE public.organizations ADD facebook_pixel Integer;