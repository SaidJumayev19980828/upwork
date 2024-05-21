--liquibase formatted sql

--changeset Moamen:review-likes dbms:postgresql splitStatements:true failOnError:true

ALTER TABLE post_likes ADD COLUMN review_id BIGINT;
ALTER TABLE post_likes ADD CONSTRAINT FK_LIKE_REVIEW_ID FOREIGN KEY (review_id) REFERENCES public.posts (id);

