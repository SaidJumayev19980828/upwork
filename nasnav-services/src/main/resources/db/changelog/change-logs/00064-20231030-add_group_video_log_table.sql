--liquibase formatted sql

--changeset ihab:add_group_video_log dbms:postgresql splitStatements:false failOnError:true

--comment: add_group_video_chat_log


create Table group_video_chat_logs (
         id bigserial not null Primary Key,
         created_at timestamp without time zone NOT null default now(),
         description text null,
         token text not null,
         organization_id bigint not null References Organizations(id),
         is_active boolean default true,
         status integer not null default 1,
         name text NOT NULL
);
