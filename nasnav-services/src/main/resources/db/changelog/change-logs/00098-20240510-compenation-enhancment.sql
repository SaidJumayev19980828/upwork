--liquibase formatted sql

--changeset Moamen:compensation_rules_unique_constraint dbms:postgresql splitStatements:true failOnError:true


ALTER TABLE compensation_rules
DROP CONSTRAINT IF EXISTS uc_action_organization_unique;

ALTER TABLE compensation_rules
ADD COLUMN IF NOT EXISTS description VARCHAR(255);
