--liquibase formatted sql

--changeset Diaa:user_otp_table dbms:postgresql splitStatements:false failOnError:true

--comment: create employee user otp table

-- modify id column with autogenerate by sqe ->
ALTER TABLE ONLY public.employee_user_otp
ALTER COLUMN id SET DEFAULT nextval('public.employee_user_otp_seq'::regclass);