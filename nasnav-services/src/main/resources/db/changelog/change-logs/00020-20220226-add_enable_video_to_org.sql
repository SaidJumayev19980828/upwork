--liquibase formatted sql

--changeset bassam:add_enable_video_to_org dbms:postgresql splitStatements:false failOnError:true

--comment: add_enable_video_to_org

ALTER TABLE public.organizations ADD COLUMN enable_video_chat integer NOT NULL DEFAULT 0;