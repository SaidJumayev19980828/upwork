--liquibase formatted sql

--changeset Hussien:add-event_id-to-room dbms:postgresql splitStatements:false failOnError:true

--comment: add event_id to room

ALTER TABLE public.room_templates ADD event_id BIGINT NULL REFERENCES public.events(id);

ALTER TABLE public.room_templates ALTER COLUMN shop_id DROP NOT NULL;

ALTER TABLE public.room_sessions ALTER COLUMN user_creator DROP NOT NULL;