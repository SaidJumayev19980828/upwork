--liquibase formatted sql

--changeset Mohamed Ismail:notification_topics and  dbms:postgresql splitStatements:false failOnError:true

--comment: add manyTomany table for notification_topics and emplyeeUser also alter user_tokens with notification_token


CREATE TABLE notification_topics
(
id bigint NOT NULL, 
topic VARCHAR NOT NULL,
PRIMARY KEY(id)
);


ALTER TABLE user_tokens ADD notification_token text;

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
