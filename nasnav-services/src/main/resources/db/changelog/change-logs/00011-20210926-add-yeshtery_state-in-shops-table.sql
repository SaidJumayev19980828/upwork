--liquibase formatted sql

--changeset Salam:add-yeshtery_state_in_shops_table dbms:postgresql splitStatements:false failOnError:true

--comment: add yeshtery state flag to filter shops by yeshtery state

alter table public.shops add column yeshtery_state int4;