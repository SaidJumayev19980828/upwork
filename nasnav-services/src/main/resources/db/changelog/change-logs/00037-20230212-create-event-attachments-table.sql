--liquibase formatted sql

--changeset mohamed ismail:create-event-attachments-table dbms:postgresql splitStatements:false failOnError:true

--comment: create tables (event_attachments, influencer_categoies) for events and influencers


create TABLE event_attachments(
                       id bigserial not null Primary Key,
                       event_id bigint not null References events(id),
                       url text,
                       type text
);

create TABLE influencer_categories(
                       id bigserial not null Primary Key,
                       influencer_id bigint not null References influencers(id),
                       category_id bigint not null References categories(id)
);
