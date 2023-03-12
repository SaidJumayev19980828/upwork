--liquibase formatted sql

--changeset Hussien:add_user_id_constraint_to_otp dbms:postgresql splitStatements:false failOnError:true

--comment: add user_id constraint to otp


ALTER TABLE public.user_otp ADD CONSTRAINT user_otp_user_fk FOREIGN KEY (user_id) REFERENCES public.users(id);