-- liquibase formatted sql
--changeset Moamen:referral_wallet dbms:postgresql splitStatements:false failOnError:true

--comment: referral_wallet table
CREATE TABLE referral_wallet (
    id bigserial PRIMARY KEY,
    balance NUMERIC  NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP NOT NULL  DEFAULT now(),
    version INT,
    user_id BIGINT UNIQUE NOT NULL,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE referral_wallet_transactions (
    id bigserial PRIMARY KEY,
    amount NUMERIC NOT NULL,
    created_at TIMESTAMP NOT NULL  DEFAULT now(),
    type VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    referral_wallet_id BIGINT NOT NULL,
    CONSTRAINT fk_referral_wallet_id FOREIGN KEY (referral_wallet_id) REFERENCES referral_wallet (id) ON DELETE CASCADE
);
