--liquibase formatted sql

--changeset MohamedShaker:organizations dbms:postgresql splitStatements:false failOnError:true

--comment: ADD name and constraint only as text

ALTER TABLE IF EXISTS public.orders ADD COLUMN created_by_employee_id bigint;


