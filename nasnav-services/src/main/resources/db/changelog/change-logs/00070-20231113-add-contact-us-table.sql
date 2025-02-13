--liquibase formatted sql

--changeset Moamen:contact_us dbms:postgresql splitStatements:false failOnError:true

--comment: add contact_us table

CREATE TABLE contact_us (
    id  bigint generated by default as identity primary key,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    organization_id bigint REFERENCES organizations(id)
);