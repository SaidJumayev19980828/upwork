--liquibase formatted sql

--changeset ihab:add_group_video_log_employee_user_update_ref dbms:postgresql splitStatements:false failOnError:true

--comment: add_group_video_chat_log_employee_user_update_ref

ALTER TABLE group_video_chat_logs
    ADD CONSTRAINT FK_GROUP_VIDEO_CHAT_LOGS_ON_SHOP FOREIGN KEY (shop_id) REFERENCES shops (id);

ALTER TABLE group_video_chat_log_employee_user
    ADD CONSTRAINT fk_grovidchalogempuse_on_employee_user_entity FOREIGN KEY (employee_user_id) REFERENCES employee_users (id);

ALTER TABLE group_video_chat_log_employee_user
    ADD CONSTRAINT fk_grovidchalogempuse_on_group_video_chat_log_entity FOREIGN KEY (group_video_chat_log_id) REFERENCES group_video_chat_logs (id);

ALTER TABLE group_video_chat_log_user
    ADD CONSTRAINT fk_grovidchaloguse_on_group_video_chat_log_entity FOREIGN KEY (group_video_chat_log_id) REFERENCES group_video_chat_logs (id);

ALTER TABLE group_video_chat_log_user
    ADD CONSTRAINT fk_grovidchaloguse_on_user_entity FOREIGN KEY (user_id) REFERENCES users (id);
