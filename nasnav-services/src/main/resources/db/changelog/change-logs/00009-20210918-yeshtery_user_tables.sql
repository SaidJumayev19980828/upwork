--liquibase formatted sql

--changeset bassam:add-yeshtery_user_tables dbms:postgresql splitStatements:false failOnError:true

--comment: add yeshtery user tables

CREATE TABLE public.yeshtery_users (
                                       id bigserial NOT NULL,
                                       email varchar NOT NULL DEFAULT ''::character varying,
                                       encrypted_password varchar NOT NULL DEFAULT ''::character varying,
                                       reset_password_token varchar NULL,
                                       reset_password_sent_at timestamp NULL,
                                       remember_created_at timestamp NULL,
                                       sign_in_count int4 NOT NULL DEFAULT 0,
                                       current_sign_in_at timestamp NULL,
                                       last_sign_in_at timestamp NULL,
                                       current_sign_in_ip inet NULL,
                                       last_sign_in_ip inet NULL,
                                       user_name varchar NULL,
                                       avatar varchar NULL,
                                       gender varchar NULL,
                                       birth_date varchar NULL,
                                       authentication_token varchar NULL DEFAULT ''::character varying,
                                       address varchar NULL,
                                       phone_number varchar NULL,
                                       post_code varchar NULL,
                                       image text NULL,
                                       oauth_token varchar NULL,
                                       oauth_expires_at timestamp NULL,
                                       organization_id int8 NULL,
                                       mobile text NULL,
                                       user_status int4 NOT NULL DEFAULT 0,
                                       first_name text NULL,
                                       last_name text NULL,
                                       allow_reward bool NOT NULL DEFAULT true,
                                       tier_created_at timestamp NULL,
                                       booster_id int8 NULL,
                                       booster_created timestamp NULL,
                                       date_of_birth timestamp NULL,
                                       family_id int8 NULL,
                                       tier_id int8 NULL,
                                       CONSTRAINT yeshtery_users_pkey_1 PRIMARY KEY (id)
);




CREATE TABLE public.yeshtery_user_addresses (
                                                id bigserial NOT NULL,
                                                yeshtery_user_id int8 NOT NULL,
                                                address_id int8 NOT NULL,
                                                principal bool NOT NULL DEFAULT false,
                                                CONSTRAINT yeshtery_user_addresses_pkey PRIMARY KEY (id),
                                                CONSTRAINT yeshtery_user_addresses_address_id_fkey FOREIGN KEY (address_id) REFERENCES public.addresses(id) ON DELETE CASCADE,
                                                CONSTRAINT yeshtery_user_addresses_user_id_fkey FOREIGN KEY (yeshtery_user_id) REFERENCES public.yeshtery_users(id)
);



CREATE TABLE public.yeshtery_user_tokens (
                                             id bigserial NOT NULL,
                                             "token" text NOT NULL,
                                             update_time timestamp NULL,
                                             yeshtery_user_id int8 NULL,
                                             employee_user_id int8 NULL,
                                             CONSTRAINT yeshtery_user_tokens_pkey PRIMARY KEY (id),
                                             CONSTRAINT yeshtery_user_tokens_token_key UNIQUE (token),
                                             CONSTRAINT yeshtery_user_tokens_employee_user_id_fkey FOREIGN KEY (employee_user_id) REFERENCES public.employee_users(id),
                                             CONSTRAINT yeshtery_user_tokens_user_id_fkey FOREIGN KEY (yeshtery_user_id) REFERENCES public.yeshtery_users(id)
);

