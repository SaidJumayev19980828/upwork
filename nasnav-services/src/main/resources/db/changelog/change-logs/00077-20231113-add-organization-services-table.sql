--liquibase formatted sql

--changeset Assem:add organization services table dbms:postgresql splitStatements:false failOnError:true

--comment: organization services table

CREATE TABLE organization_services
(
    id         bigserial NOT NULL,
    org_id     BIGINT,
    service_id BIGINT,
    enabled    BOOLEAN,
    CONSTRAINT pk_organization_services PRIMARY KEY (id)
);