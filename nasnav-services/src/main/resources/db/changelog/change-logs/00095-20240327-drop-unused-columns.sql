--liquibase formatted sql

--changeset Moamen:drop-unused-columns-for-post-entity splitStatements:true failOnError:true


ALTER TABLE post_likes DROP COLUMN IF EXISTS post_id;
