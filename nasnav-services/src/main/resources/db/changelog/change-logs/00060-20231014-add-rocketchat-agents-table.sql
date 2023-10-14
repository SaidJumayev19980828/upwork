--liquibase formatted sql

--changeset Hussien:rocket_chat_employee_agents dbms:postgresql splitStatements:false failOnError:true

--comment: add rocket_chat_employee_agents table

CREATE TABLE public.rocket_chat_employee_agents(
    employee_id bigint not null Primary Key References public.employee_users(id),
    agent_id varchar not null
);