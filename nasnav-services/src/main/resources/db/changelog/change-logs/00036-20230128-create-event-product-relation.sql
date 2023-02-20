--liquibase formatted sql

--changeset mohamed ismail:create-event-products-table dbms:postgresql splitStatements:false failOnError:true

--comment: create tables events products relationship

CREATE TABLE event_products(
                               id bigserial not null Primary Key,
                               event_id bigint not null References events(id),
                               product_id bigint not null References products(id)
);