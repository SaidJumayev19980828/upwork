--liquibase formatted sql

--changeset abdelsalam:change-fb-pixel-to-text dbms:postgresql splitStatements:false failOnError:true

--comment: change facebook pixel type to text

alter table organizations alter column facebook_pixel type text;