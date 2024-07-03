--liquibase formatted sql

--changeset ihab:update-nasnav-admin-role dbms:postgresql splitStatements:false failOnError:true

UPDATE public.roles SET name = 'NASNAV_ADMIN' WHERE name = 'MEETUSVR_ADMIN';
UPDATE public.roles SET name = 'NASNAV_EMPLOYEE' WHERE name = 'MEETUSVR_EMPLOYEE';