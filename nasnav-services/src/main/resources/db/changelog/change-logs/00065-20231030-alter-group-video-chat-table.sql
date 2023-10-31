--liquibase formatted sql

--changeset ihab:alter-group-video-chat-log-table dbms:postgresql splitStatements:false failOnError:true

--comment: add shop_id and ended_at to group video chat logs table

ALTER TABLE group_video_chat_logs ADD COLUMN shop_id bigint REFERENCES public.shops(id);
ALTER TABLE group_video_chat_logs ADD ended_at timestamp without time zone;