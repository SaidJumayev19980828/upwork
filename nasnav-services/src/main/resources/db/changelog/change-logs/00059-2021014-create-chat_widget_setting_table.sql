--liquibase formatted sql

--changeset Ihab:chat_widget_setting dbms:postgresql splitStatements:false failOnError:true

--comment: create chat widget setting table

CREATE TABLE chat_widget_setting(
                                    id bigserial not null Primary Key,
                                    org_id bigint not null References Organizations(id),
                                    value text  not null,
                                    type int4  not null
);