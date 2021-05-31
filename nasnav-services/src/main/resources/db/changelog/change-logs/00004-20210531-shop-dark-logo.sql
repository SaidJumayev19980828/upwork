--liquibase formatted sql

--changeset abdelsalam:add-shop-dark-logo dbms:postgresql splitStatements:false failOnError:true

--comment: rename not used column [view_image] to dark_logo to be used by frontend
ALTER TABLE public.shops RENAME view_image TO dark_logo;