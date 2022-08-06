--liquibase formatted sql

--changeset sameh-adel:add-ended-date-to-video-chat-log dbms:postgresql splitStatements:false failOnError:true

--comment: add ended date to video chat log

ALTER TABLE public.video_chat_logs ADD ended_at timestamp without time zone;