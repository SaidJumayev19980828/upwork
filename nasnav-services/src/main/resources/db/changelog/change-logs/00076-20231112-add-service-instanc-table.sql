--liquibase formatted sql

--changeset Assem:add service instance table dbms:postgresql splitStatements:false failOnError:true

--comment: service instance table

CREATE TABLE service_instance
(
    id          bigserial NOT NULL,
    package_id  BIGINT,
    service_id  BIGINT,
    name        VARCHAR(255),
    description VARCHAR(255),
    CONSTRAINT pk_service_instance PRIMARY KEY (id)
);

ALTER TABLE service_instance
    ADD CONSTRAINT FK_SERVICE_INSTANCE_ON_PACKAGE FOREIGN KEY (package_id) REFERENCES package (id);