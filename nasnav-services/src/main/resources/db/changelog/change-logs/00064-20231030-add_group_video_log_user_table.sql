--liquibase formatted sql

--changeset ihab:add_group_video_log_user dbms:postgresql splitStatements:false failOnError:true

--comment: add_group_video_chat_log_user


CREATE TABLE group_video_chat_log_user (
                                           id bigint NOT NULL,
                                           user_id bigint NOT NULL,
                                           group_video_chat_log_id bigint NOT NULL,
                                           principal boolean DEFAULT false NOT NULL
);
