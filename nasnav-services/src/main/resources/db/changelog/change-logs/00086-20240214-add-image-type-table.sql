--liquibase formatted sql
--changeset khalid:adding organization_images_types
create table organization_images_types(
    type_id BIGINT NOT NULL ,
    organization_id BIGINT NOT NULL ,
    label TEXT,
    text  TEXT,
    CONSTRAINT pk_organization_images_types PRIMARY KEY (type_id)
);