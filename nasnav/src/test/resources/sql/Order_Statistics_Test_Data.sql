INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');

INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 102, 99001, 0);
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (100, 'product_1',102, 201, 99001, now(), now());

insert into public.product_variants(id, "name" , product_id, removed ) values(310001, 'var' 	, 100, 1);

--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 502, 6, 99001, 600.00, 310001);

INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001);


INSERT INTO public.meta_orders(id, created_at, user_id, organization_id, status) VALUES(310001 , now(),88, 99001, 8);

INSERT INTO public.orders
(id,address, "name", user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, total, meta_order_id)
VALUES(330002, '', '', 88, '2022-02-01', now(), now(), 99001, 1, '{}'::character varying[], 502, '{}'::text, 600.00, 0, 600.00, 310001);


INSERT INTO public.orders
(id,address, "name", user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, total, meta_order_id)
VALUES(330003, '', '', 88, '2022-02-02', now() + interval '2 day', now(), 99001, 0, '{}'::character varying[], 502, '{}'::text, 300.00, 0, 300.00, 310001);


INSERT INTO public.orders
(id,address, "name", user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, total, meta_order_id)
VALUES(330004, '', '', 88, '2022-02-03', now() + interval '1 day', now(), 99002, 0, '{}'::character varying[], 502, '{}'::text, 200.00, 0, 200.00, 310001);

INSERT INTO public.orders
(id,address, "name", user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, total, meta_order_id)
VALUES(330005, '', '', 88, '2022-02-04', now() + interval '3 day', now(), 99002, 0, '{}'::character varying[], 502, '{}'::text, 50.00, 0, 50.00, 310001);


INSERT INTO public.orders
(id,address, "name", user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, total, meta_order_id)
VALUES(330006, '', '', 88, '2022-02-05', now() + interval '4 day', now(), 99003, 1, '{}'::character varying[], 502, '{}'::text, 100.00, 0, 100.00, 310001);



INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330002, 601, 14, 600.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330003, 601, 7, 300.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency,discount)VALUES(330004, 601, 5, 200.0, 1, 100);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330005, 601, 1, 50.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(330006, 601, 3, 100.0, 1);

