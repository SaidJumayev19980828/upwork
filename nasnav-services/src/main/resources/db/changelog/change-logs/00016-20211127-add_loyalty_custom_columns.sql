--liquibase formatted sql

--changeset Bassam:add_loyalty_custom_columns dbms:postgresql splitStatements:false failOnError:true

--comment: add_loyalty_custom_columns
alter table yeshtery_users add column referral varchar ;