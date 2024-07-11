-- liquibase formatted sql
--changeset Moamen:bank_outside_transactions dbms:postgresql splitStatements:false failOnError:true

--comment: Add unique constraint to bank_outside_transactions table

-- ALTER TABLE bank_outside_transactions ADD CONSTRAINT uc_bank_outside_transactions_bc_key UNIQUE (bc_key);
