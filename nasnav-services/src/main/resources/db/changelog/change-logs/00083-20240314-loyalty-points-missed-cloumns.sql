-- liquibase formatted sql
--changeset Moamen:user_loyalty_transactions dbms:postgresql splitStatements:false failOnError:true

--comment: user_loyalty_transactions table
ALTER TABLE user_loyalty_transactions
ADD COLUMN order_id bigint REFERENCES orders(id),
ADD COLUMN shop_id bigint REFERENCES shops(id),
ADD COLUMN org_id bigint REFERENCES organizations(id);
