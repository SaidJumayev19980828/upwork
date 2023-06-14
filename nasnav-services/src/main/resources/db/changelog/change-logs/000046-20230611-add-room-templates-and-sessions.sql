--liquibase formatted sql

--changeset Hussien:add-room-templates-and-sessions dbms:postgresql splitStatements:false failOnError:true

--comment: creates room_templates and room_sessions tables for metaverse

CREATE TABLE room_templates (
	id BIGSERIAL NOT NULL PRIMARY KEY,
	shop_id BIGINT NOT NULL REFERENCES public.shops(id),
	scene_id TEXT NOT NULL,
	data TEXT NOT NULL
);

CREATE TABLE room_sessions (
	id BIGSERIAL NOT NULL PRIMARY KEY,
	template_id BIGINT NOT NULL REFERENCES public.room_templates(id),
	external_id TEXT NOT NULL,
	created_at TIMESTAMP NOT NULL,
	user_creator BIGINT NOT NULL REFERENCES public.users(id)
);
