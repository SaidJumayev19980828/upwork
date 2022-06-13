--liquibase formatted sql

--changeset bassam:add_enable_video_to_org dbms:postgresql splitStatements:false failOnError:true

--comment: add_enable_video_to_org


create Table video_chat_logs (
         id bigserial not null Primary Key,
         created_at timestamp without time zone NOT null default now(),
         description text null,
         token text not null,
         organization_id bigint not null References Organizations(id),
         user_id bigint not null References users(id),
         assigned_to_id bigint not null References users(id),
         is_active boolean default true,
         status integer not null default 1
);
