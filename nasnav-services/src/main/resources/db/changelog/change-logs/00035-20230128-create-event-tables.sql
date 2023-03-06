--liquibase formatted sql

--changeset mohamed ismail:create-events-table dbms:postgresql splitStatements:false failOnError:true

--comment: create tables (events, event_logs, influencers, event_requests) for events

CREATE TABLE public.influencers(
                                   id bigserial not null Primary Key,
                                   created_at timestamp without time zone NOT null default now(),
                                   employee_user_id bigint null References employee_users(id),
                                   user_id bigint null References users(id),
                                   approved BOOLEAN not null default false
);

create TABLE events(
                       id bigserial not null Primary Key,
                       created_at timestamp without time zone NOT null default now(),
                       starts_at timestamp without time zone NOT null default now(),
                       ends_at timestamp without time zone NOT null default now(),
                       organization_id bigint not null References Organizations(id),
                       influencer_id bigint null References influencers(id),
                       visible BOOLEAN not null default false,
                       name text,
                       description text,
                       status integer not null default (0)

);

CREATE TABLE event_requests(
                               id bigserial not null Primary Key,
                               created_at timestamp without time zone NOT null default now(),
                               starts_at timestamp without time zone NOT null default now(),
                               ends_at timestamp without time zone NOT null default now(),
                               event_id bigint not null References events(id),
                               influencer_id bigint not null References influencers(id),
                               status int not null default 0
);

CREATE TABLE event_logs(
                           id bigserial not null Primary Key,
                           created_at timestamp without time zone NOT null default now(),
                           interested_at timestamp without time zone null,
                           attend_at timestamp without time zone null,
                           event_id bigint not null References events(id),
                           user_id bigint  null References users(id),
                           employee_id bigint null References employee_users(id)
);
