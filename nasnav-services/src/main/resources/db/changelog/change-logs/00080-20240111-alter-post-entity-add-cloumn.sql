-- liquibase formatted sql

--changeset Moamen:posts dbms:postgresql splitStatements:false failOnError:true

--comment: add Shop and Ratings and product name for posts Entity

ALTER TABLE posts ADD COLUMN IF NOT EXISTS shop bigint REFERENCES shops(id);
ALTER TABLE posts ADD COLUMN IF NOT EXISTS ratings SMALLINT;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS product_name VARCHAR(255);

