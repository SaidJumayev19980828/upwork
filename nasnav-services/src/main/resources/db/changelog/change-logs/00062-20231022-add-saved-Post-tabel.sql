--liquibase formatted sql

--changeset Moamen:saved_posts dbms:postgresql splitStatements:false failOnError:true

--comment: add saved_posts

CREATE TABLE saved_posts(
                               id bigserial not null Primary Key,
                               post_id bigint not null References posts(id),
                               user_id bigint not null References users(id)
);