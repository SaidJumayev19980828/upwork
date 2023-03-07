--liquibase formatted sql

--changeset Doaa:add-addons dbms:postgresql splitStatements:false failOnError:true

--comment: add-addons

CREATE TABLE public.addons (
    id  SERIAL PRIMARY KEY NOT NULL,
     type integer DEFAULT 1 NOT NULL,
    name text NOT NULL,
    
    organization_id bigint NOT NULL
    
);

----------------------------------------------
CREATE TABLE public.product_addons(
    product_id bigint NOT NULL,
    addon_id bigint NOT NULL
);


-------------------------------------

CREATE TABLE public.addon_stocks(
    id  SERIAL PRIMARY KEY NOT NULL,
    shop_id bigint,
	addon_id bigint,
    quantity integer,
    price numeric(10,2) DEFAULT 0
    
);
-------------------------------------------------

CREATE TABLE public.cart_item_addon_details(
    id  SERIAL PRIMARY KEY NOT NULL,
    addon_stock_id bigint,
	cart_item_id bigint,
    quantity integer
    user_id bigint,
    
);
--------------------------------------------

ALTER TABLE public.baskets ADD COLUMN  addon_price numeric(10,2) DEFAULT 0;

