--liquibase formatted sql

--changeset bassam:update_paymob_wallet_test_identifier dbms:postgresql splitStatements:false failOnError:true

--comment: update_paymob_wallet_test_identifier

update paymob_source set identifier = '01010101010' where value = '1928804';

