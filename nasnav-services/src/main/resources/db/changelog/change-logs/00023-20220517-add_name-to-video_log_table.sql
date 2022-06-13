--liquibase formatted sql

--changeset abdel-salam:add_name_to_video_log_table dbms:postgresql splitStatements:false failOnError:true

--comment: add missing property [name] to video_chat_logs table

ALTER TABLE public.video_chat_logs ADD name text NOT NULL;