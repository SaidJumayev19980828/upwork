--liquibase formatted sql

--changeset Hussien:add_room_session_status dbms:postgresql splitStatements:false failOnError:true

--comment: add room session status

ALTER TABLE public.room_sessions ADD COLUMN status VARCHAR NOT NULL DEFAULT 'STARTED';

