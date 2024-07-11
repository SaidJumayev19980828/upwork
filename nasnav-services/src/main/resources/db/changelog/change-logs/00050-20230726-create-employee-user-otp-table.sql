--liquibase formatted sql

--changeset Diaa:user_otp_table dbms:postgresql splitStatements:false failOnError:true

--comment: create employee user otp table

CREATE TABLE public.employee_user_otp (
                                          otp character varying COLLATE pg_catalog."default" NOT NULL,
                                          id bigint NOT NULL ,
                                          user_id bigint NOT NULL,
                                          created_at timestamp without time zone NOT NULL,
                                          type character varying COLLATE pg_catalog."default" NOT NULL,
                                          CONSTRAINT employee_user_otp_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS public.employee_user_otp_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER TABLE public.employee_user_otp_seq OWNER TO postgres;

ALTER SEQUENCE public.employee_user_otp_seq
    OWNED BY public.employee_user_otp.id;


CREATE INDEX index_user_id_on_employee_user_otp_id ON public.employee_user_otp USING btree (user_id);


ALTER TABLE ONLY public.employee_user_otp
    ADD CONSTRAINT fk_employee_user_otp_emplouee_user_id FOREIGN KEY (user_id)
    REFERENCES public.employee_users(id);
    
ALTER TABLE ONLY public.employee_user_otp ADD COLUMN attempts BIGINT NOT NULL DEFAULT 0;
