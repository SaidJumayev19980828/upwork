--liquibase formatted sql

--changeset Eslam:add notification_topics dbms:postgresql splitStatements:false failOnError:true

--comment: add manyTomany table for notification_topics and emplyeeUser and also add notification_topic to organizations

CREATE TABLE notification_topics
(
    id bigserial NOT NULL,
    topic VARCHAR NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE topic_employee_users
(
    employee_user_id bigint,
    topic_id bigint,
    PRIMARY KEY(employee_user_id, topic_id)
);

alter table topic_employee_users
    add constraint
        fk_notification__topic foreign key(topic_id) references notification_topics(id);

alter table topic_employee_users
    add constraint
        fk_notification__Employee foreign key(employee_user_id) references employee_users(id);

ALTER TABLE organizations ADD notification_topic bigint;

alter table organizations
    add constraint
        fk_notification__Orgranization foreign key(notification_topic) references notification_topics(id);

ALTER TABLE shops ADD notification_topic bigint;

alter table shops
    add constraint
        fk_notification__shops foreign key(notification_topic) references notification_topics(id);

