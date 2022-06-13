--liquibase formatted sql

--changeset bassam:add_employee_reference_instead_of_user dbms:postgresql splitStatements:false failOnError:true

--comment: add reference to employee instead of user reference

alter table  video_chat_logs    drop column    assigned_to_id;
alter table  video_chat_logs add  column    assigned_to_id bigint References employee_users(id);

