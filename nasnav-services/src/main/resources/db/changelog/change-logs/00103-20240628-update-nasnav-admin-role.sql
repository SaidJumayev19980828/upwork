--liquibase formatted sql

--changeset ihab:update-nasnav-admin-role dbms:postgresql splitStatements:false failOnError:true

UPDATE public.roles SET name = 'MEETUSVR_ADMIN' WHERE name = 'NASNAV_ADMIN';
UPDATE public.roles SET name = 'MEETUSVR_EMPLOYEE' WHERE name = 'NASNAV_EMPLOYEE';