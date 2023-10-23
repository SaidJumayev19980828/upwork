--liquibase formatted sql

--changeset Ihab:delete-data-from-package-table dbms:postgresql splitStatements:false failOnError:true

--comment: delete data from package table

DELETE From public.package;
