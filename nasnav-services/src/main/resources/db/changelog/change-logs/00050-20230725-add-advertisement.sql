--liquibase formatted sql

--changeset Ashraf:add_advertisements dbms:postgresql splitStatements:false failOnError:true

--comment: add advertisements

create table if not exists public.advertisements
(
    id         bigint generated by default as identity primary key,
    coins      integer,
    created_at timestamp,
    from_date  timestamp,
    to_date    timestamp,
    likes      integer,
    product_id bigint not null
        constraint advertisements_product_fk references public.products
);

create table if not exists public.post_transactions
(
    id               bigint generated by default as identity primary key,
    paid_coins       bigint,
    transaction_date timestamp,
    post_id          bigint not null
        constraint post_transactions_post_fk references public.posts
);

alter table public.posts
    add column if not exists advertisement_id bigint
        constraint posts_advertisements_fk references public.advertisements;
