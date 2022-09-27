--liquibase formatted sql

--changeset abdel-salaml:add-shop_id-to-video-chat dbms:postgresql splitStatements:false failOnError:true

--comment: add shop_id to video chat logs table

ALTER TABLE video_chat_logs ADD COLUMN shop_id bigint REFERENCES public.shops(id);