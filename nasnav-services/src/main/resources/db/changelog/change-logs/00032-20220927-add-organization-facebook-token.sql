--liquibase formatted sql

--changeset abdelsalam:add-organization-facebook-token dbms:postgresql splitStatements:false failOnError:true

--comment: add facebook token column for organization

ALTER TABLE organizations ADD column facebook_token text null;

