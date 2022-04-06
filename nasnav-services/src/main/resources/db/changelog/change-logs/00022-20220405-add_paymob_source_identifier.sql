--liquibase formatted sql

--changeset bassam:add_paymob_source_identifier dbms:postgresql splitStatements:false failOnError:true

--comment: add_paymob_source_identifier

ALTER TABLE public.paymob_source ADD identifier varchar;


-- test data
update public.paymob_source set identifier = 'Wallet', type ='+201110002373' where value = '1739267';
update public.paymob_source set identifier = 'AGGREGATOR', type ='AGGREGATOR' where value = '1928804';
update public.paymob_source set identifier = 'cash', type ='AGGREGATOR' where value = '1974407';

ALTER TABLE public.paymob_source ALTER COLUMN identifier SET NOT NULL;
