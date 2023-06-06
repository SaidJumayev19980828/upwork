-- liquibase formatted sql

--changeset mahmoud:add-files_user_id_reference_in_files_table dbms:postgresql splitStatements:false failOnError:true

ALTER TABLE public.files ADD COLUMN user_id BIGINT;


-- Add a foreign key constraint to link the "user_id" column to the "id" column of the "users" table
ALTER TABLE public.files ADD CONSTRAINT fk_files_user_id FOREIGN KEY (user_id) REFERENCES public.users (id);