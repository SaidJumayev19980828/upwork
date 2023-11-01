--liquibase formatted sql

--changeset Mark:add services dbms:postgresql splitStatements:false failOnError:true

--comment: add services

--inserting service
INSERT INTO public.service(id,code,name,description) values (13,'AI_VIRTUAL_ASSISTANT','AI_VIRTUAL_ASSISTANT','AI_VIRTUAL_ASSISTANT Service');