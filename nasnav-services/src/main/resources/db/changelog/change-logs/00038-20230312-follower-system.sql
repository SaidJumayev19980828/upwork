--liquibase formatted sql

--changeset mohamed ismail:create-follower-user-table dbms:postgresql splitStatements:false failOnError:true

--comment: create follower_user for for you feature

CREATE TABLE public.user_followers(
                                   id bigserial not null Primary Key,
                                   user_id bigint not null References users(id),
                                   follower_id bigint not null References users(id)
);

CREATE TABLE public.posts(
                             id bigserial not null Primary Key,
                             user_id bigint not null References users(id),
                             org_id bigint not null References organizations(id),
                             description text,
                             type INTEGER not null default 0,
                             status INTEGER NOT NULL default 0,
                             created_at timestamp without time zone NOT null default now()

);

CREATE TABLE post_products(
                              id bigserial not null Primary Key,
                              post_id bigint not null References posts(id),
                              product_id bigint not null References products(id)
);

create TABLE post_attachments(
                                 id bigserial not null Primary Key,
                                 post_id bigint not null References posts(id),
                                 url text,
                                 type text
);

create TABLE post_likes(
                           id bigserial not null Primary Key,
                           post_id bigint not null References posts(id),
                           user_id bigint not null References users(id),
                           created_at timestamp without time zone NOT null default now()

);

create TABLE post_clicks(
                            id bigserial not null Primary Key,
                            post_id bigint not null References posts(id),
                            user_id bigint not null References users(id),
                            created_at timestamp without time zone NOT null default now(),
                            clicks_count INTEGER not null default 1

);

