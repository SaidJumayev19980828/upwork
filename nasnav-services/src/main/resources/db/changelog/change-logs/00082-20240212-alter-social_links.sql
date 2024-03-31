-- --liquibase formatted sql
--
-- --changeset Khalid:add tiktok column
--
-- --comment: adding tiktok column at social_links table
ALTER TABLE social_links add column tiktok TEXT NULL ;