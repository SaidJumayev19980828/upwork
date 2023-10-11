--liquibase formatted sql

--changeset Moamen:event_influencers dbms:postgresql splitStatements:false failOnError:true

--comment: add event_influencers

CREATE TABLE event_influencers(
                               id bigserial not null Primary Key,
                               event_id bigint not null References events(id),
                               influencer_id bigint not null References influencers(id)
);


ALTER TABLE events DROP COLUMN influencer_id ;
