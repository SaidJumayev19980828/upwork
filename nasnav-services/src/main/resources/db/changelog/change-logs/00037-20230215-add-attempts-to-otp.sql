--liquibase formatted sql

--changeset Hussien:add-attempts-to-otp dbms:postgresql splitStatements:false failOnError:true

--comment: added attempts column to OTP

ALTER TABLE ONLY public.user_otp ADD COLUMN attempts BIGINT NOT NULL DEFAULT 0;