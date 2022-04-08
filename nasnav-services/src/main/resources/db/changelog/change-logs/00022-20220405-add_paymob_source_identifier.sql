--liquibase formatted sql

--changeset bassam:add_paymob_source_identifier dbms:postgresql splitStatements:false failOnError:true

--comment: add_paymob_source_identifier

ALTER TABLE public.paymob_source ADD identifier varchar;


-- test data
    update public.paymob_source set identifier = 'Card', type ='Card' where value = '1739267';
update public.paymob_source set identifier = '+201110002373', type ='WALLET' where value = '1928804';
update public.paymob_source set identifier = 'cash', type ='Cash Collection' where value = '1974407';

ALTER TABLE public.paymob_source ALTER COLUMN identifier SET NOT NULL;
