--liquibase formatted sql

--changeset ihab:add_group_video_log_employee_user dbms:postgresql splitStatements:false failOnError:true

--comment: add_group_video_chat_log_employee_user


CREATE TABLE group_video_chat_log_employee_user (
                                                    id bigint NOT NULL,
                                                    employee_user_id bigint NOT NULL,
                                                    group_video_chat_log_id bigint NOT NULL,
                                                    principal boolean DEFAULT false NOT NULL
);