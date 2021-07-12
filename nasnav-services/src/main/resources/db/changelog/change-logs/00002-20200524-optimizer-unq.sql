--liquibase formatted sql

--changeset galal:unique-index-for-default-optimizer dbms:postgresql splitStatements:false failOnError:true

--comment: POSTGRES doesn't consider unique null value in indices,so the combination (optimization_strategy = X, organization_id = Y, shipping_service_id=null) may be repeated, which means an organization may have multiple default optimizations strategies

CREATE UNIQUE INDEX organiztion_cart_optimization_default_strategy_idx ON public.organiztion_cart_optimization USING btree (optimization_strategy, organization_id, (shipping_service_id IS NULL));