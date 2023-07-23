--liquibase formatted sql

--changeset Hussien:add_gender_to_employee_user dbms:postgresql splitStatements:false failOnError:true

--comment: add gender to employee users

ALTER TABLE employee_users ADD COLUMN if not EXISTS gender character varying NULL;
