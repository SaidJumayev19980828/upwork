--liquibase formatted sql

--changeset Hussien Assem:create rocket_chat_customer_tokens table dbms:postgresql splitStatements:false failOnError:true

--comment: creates rocket_chat_customer_tokens table

CREATE TABLE public.rocket_chat_customer_tokens(
		id bigserial not null Primary Key,
		user_id bigint not null UNIQUE References users(id),
		token text not null
);
