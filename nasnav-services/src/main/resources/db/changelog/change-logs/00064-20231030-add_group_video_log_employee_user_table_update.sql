--liquibase formatted sql

--changeset ihab:add_group_video_log_employee_user_update dbms:postgresql splitStatements:false failOnError:true

--comment: add_group_video_chat_log_employee_user_update

DROP TABLE group_video_chat_log_employee_user;
CREATE TABLE group_video_chat_log_employee_user (
                                                    id bigserial not null ,
                                                    employee_user_id bigint NOT NULL,
                                                    group_video_chat_log_id bigint NOT NULL,
                                                    principal boolean DEFAULT false NOT NULL,
                                                    CONSTRAINT pk_group_video_employee PRIMARY KEY (id)
);