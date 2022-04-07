--liquibase formatted sql

--changeset bassam:add_more_paymob_source dbms:postgresql splitStatements:false failOnError:true

--comment: add_more_paymob_source



INSERT INTO public.paymob_source
(value, "name", "type", status, currency, script, icon, organization_id, identifier)
VALUES( '1974407', 'Cash Collection', 'CASH', 'Test', 'EGP', 'https://accept.paymob.com/api/acceptance/iframes/319928?payment_token={payment_key_obtained_previously}', NULL, 78, 'cash');
INSERT INTO public.paymob_source
( value, "name", "type", status, currency, script, icon, organization_id, identifier)
VALUES( '1928804', 'Mobile Wallet', 'WALLET', 'Test', 'EGP', 'https://accept.paymob.com/api/acceptance/iframes/319928?payment_token={payment_key_obtained_previously}', NULL, 78, '+201110002373');
INSERT INTO public.paymob_source
( value, "name", "type", status, currency, script, icon, organization_id, identifier)
VALUES('1739266', 'Accept Kiosk', 'AGGREGATOR', 'Test', 'EGP', 'https://accept.paymob.com/api/acceptance/iframes/319928?payment_token={payment_key_obtained_previously}', NULL, 78, 'AGGREGATOR');
