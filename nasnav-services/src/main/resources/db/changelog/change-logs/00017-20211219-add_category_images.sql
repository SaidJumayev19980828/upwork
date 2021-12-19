--liquibase formatted sql

--changeset AbdelSalam:add_category_images_columns dbms:postgresql splitStatements:false failOnError:true

--comment: add_category_images_columns

alter table categories add column cover text ;
alter table categories add column cover_small text ;