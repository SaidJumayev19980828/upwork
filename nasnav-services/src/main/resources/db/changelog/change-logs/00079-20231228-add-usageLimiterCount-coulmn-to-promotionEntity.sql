--liquibase formatted sql

--changeset omar moaz:promotions dbms:postgresql splitStatements:false failOnError:true

--comment: add usageLimiterCount to promotion table to limit the usage of the promoCode

ALTER TABLE promotions
    ADD COLUMN IF NOT EXISTS usage_limiter_count INTEGER NULL;