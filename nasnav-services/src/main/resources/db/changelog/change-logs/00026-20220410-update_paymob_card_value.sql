--liquibase formatted sql

--changeset bassam:update_paymob_card_value dbms:postgresql splitStatements:false failOnError:true

--comment: update_paymob_card_value
update paymob_source set value = '1627922' where value = '1739267';
