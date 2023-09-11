--liquibase formatted sql

--changeset ismail:add_availability_to_org dbms:postgresql splitStatements:false failOnError:true

--comment: add_availability_to_org


create Table availabilities (
                                id bigserial not null Primary Key,
                                created_at timestamp without time zone NOT null default now(),
                                starts_at timestamp without time zone NOT null default now(),
                                ends_at timestamp without time zone NOT null default now(),
                                organization_id bigint not null References Organizations(id),
                                shop_id bigint null References shops(id),
                                employee_user_id bigint not null References employee_users(id),
                                user_id bigint null References users(id)
);