--liquibase formatted sql

--changeset bassam:add_paymob_source_table dbms:postgresql splitStatements:false failOnError:true

--comment: add_paymob_source_table
create Table paymob_source (
           id bigserial not null Primary Key,
           created_at timestamp without time zone NOT null default now(),
           value varchar(50) not null,
           name text not null,
           type varchar(50) not null,
           status varchar(50) not null,
           currency varchar(3) not null,
           script text not null,
           icon text null,
           organization_id bigint not null References Organizations(id)

);

-- test data
INSERT INTO public.paymob_source
(created_at, value, "name", "type", status, currency, script, icon, organization_id)
VALUES( '2022-04-04 09:38:50.327', '1739267', 'Online Card', 'Online Card', 'Test', 'EGP', 'https://accept.paymob.com/api/acceptance/iframes/319928?payment_token={payment_key_obtained_previously}', NULL, 78);


