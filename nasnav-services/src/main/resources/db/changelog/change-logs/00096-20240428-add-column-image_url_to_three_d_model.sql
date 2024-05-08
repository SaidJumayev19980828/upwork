--liquibase formatted sql

--changeset MohamedShaker:organizations dbms:postgresql splitStatements:false failOnError:true

--comment: ADD name and constraint only as text

ALTER TABLE IF EXISTS public.product_3d_model ADD COLUMN imageUrl VARCHAR (2083);


