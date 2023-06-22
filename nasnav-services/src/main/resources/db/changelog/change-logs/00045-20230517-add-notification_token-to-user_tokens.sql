--liquibase formatted sql

--changeset Eslam:add notification_token dbms:postgresql splitStatements:false failOnError:true

--comment: add notification_token to user_tokens

ALTER TABLE user_tokens ADD notification_token text;
