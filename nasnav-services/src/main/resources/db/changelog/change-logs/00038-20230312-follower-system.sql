--liquibase formatted sql

--changeset mohamed ismail:create-follower-user-table dbms:postgresql splitStatements:false failOnError:true

--comment: create follower_user for for you feature

CREATE TABLE public.user_followers(
                                   id bigserial not null Primary Key,
                                   user_id bigint null References users(id),
                                   follower_id bigint null References users(id)
);
