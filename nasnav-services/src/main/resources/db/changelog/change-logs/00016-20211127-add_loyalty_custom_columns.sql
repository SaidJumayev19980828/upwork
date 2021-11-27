--liquibase formatted sql

--changeset Bassam:add_loyalty_custom_columns dbms:postgresql splitStatements:false failOnError:true

--comment: add_loyalty_custom_columns
alter table yeshtery_users add column referral varchar ;
alter table loyalty_point_transactions add column amount numeric ;


CREATE TABLE public.loyalty_pins (
     id bigserial NOT NULL,
     shop_id int8 NOT NULL,
     user_id int8 NOT NULL,
     created_at timestamp NOT NULL,
     pin varchar(10) NOT NULL,
     CONSTRAINT loyalty_pins_pkey PRIMARY KEY (id),
     CONSTRAINT loyalty_pins_shop_id_fkey FOREIGN KEY (shop_id) REFERENCES public.shops(id),
     CONSTRAINT loyalty_pins_user_fk FOREIGN KEY (user_id) REFERENCES public.users(id)
);
