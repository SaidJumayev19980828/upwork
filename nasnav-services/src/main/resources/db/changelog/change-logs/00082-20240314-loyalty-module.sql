-- liquibase formatted sql
--changeset Moamen:loyalty dbms:postgresql splitStatements:false failOnError:true

--comment: Add new loyalty points table

CREATE TABLE user_loyalty_points (
    id bigserial PRIMARY KEY,
    balance NUMERIC  NOT NULL DEFAULT 0.0 ,
    created_at TIMESTAMP NOT NULL  DEFAULT now(),
    version INT,
    user_id BIGINT UNIQUE NOT NULL,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT balance_positive CHECK (balance >= 0)
);

CREATE TABLE user_loyalty_transactions (
    id bigserial PRIMARY KEY,
    amount NUMERIC NOT NULL,
    created_at TIMESTAMP NOT NULL  DEFAULT now(),
    type VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    meta_order_id bigint null References meta_orders(id),
    user_loyalty_points BIGINT NOT NULL,
    CONSTRAINT fk_user_loyalty_points FOREIGN KEY (user_loyalty_points) REFERENCES user_loyalty_points (id) ON DELETE CASCADE,
    CONSTRAINT amount_positive CHECK (amount >= 0)
);
