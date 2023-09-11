--liquibase formatted sql

--changeset Hussien:add_referral_to_users dbms:postgresql splitStatements:false failOnError:true

--comment: add referral to users
alter table users add column if not EXISTS referral varchar;