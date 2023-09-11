--liquibase formatted sql

--changeset ismail:add_scheduler_tasks_to_org dbms:postgresql splitStatements:false failOnError:true

--comment: add_scheduler_tasks_to_org


create Table scheduler_tasks (
                                 id bigserial not null Primary Key,
                                 created_at timestamp without time zone NOT null default now(),
                                 starts_at timestamp without time zone NOT null default now(),
                                 availability_id bigint null References availabilities(id),
                                 user_id bigint null References users(id),
                                 employee_user_id bigint null References employee_users(id),
                                 type text null
);