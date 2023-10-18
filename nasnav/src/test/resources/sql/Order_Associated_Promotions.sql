----------------------------inserting dummy data----------------------------
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);

INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 201, 'brand_1', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Shoe size', 's-size', 'Size of the shoes', 99001);

INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300001, 'address line', 1, '01111234567');

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (501, 'shop_1', 101, 99001, 12300001);

--insering employees
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700001, '101112', now(), 68, null);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '123', now(), null, 88);

insert into roles(id, name,  organization_id) values(1, 'CUSTOMER', 99001);

--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2',101, 201, 99001, now(), now(),'123456789');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1008, 'product_8',101, 201, 99001, now(), now());


-- variants for each product
insert into public.product_variants(id, "name" , product_id, feature_spec ) values(310001, 'var' 	, 1001, '{"234":"66"
}');
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id, feature_spec ) values(310004, 'var' 	, 1004, '{"234":"45"
}');
insert into public.product_variants(id, "name" , product_id ) values(310005, 'var' 	, 1005);
insert into public.product_variants(id, "name" , product_id ) values(310006, 'var' 	, 1006);
insert into public.product_variants(id, "name" , product_id ) values(310007, 'var' 	, 1007);
insert into public.product_variants(id, "name" , product_id ) values(310008, 'var' 	, 1008);
insert into public.product_variants(id, "name" , product_id ) values(310009, 'var' 	, 1008);
insert into public.product_variants(id, "name" , product_id ) values(310010, 'var' 	, 1008);


--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(601, 501, 6, 99001, 600.00, 310001, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(602, 501, 20, 99001, 1200.0, 310002, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(603, 501, 4, 99001, 200.00, 310003, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency, discount) values(604, 501, 6, 99001, 700.00, 310004, 1, 10);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(605, 501, 0, 99001, 700.00, 310009, 0);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency, discount) values(606, 501, 10, 99001, 700.00, 310010, 1, 15);

-- insert cart
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(602, '99001/img2.jpg', '{"Color":"Blue"}', 2, 88);
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(604, '99001/cover_img.jpg', '{"Color":"Yellow"}', 3, 88);
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(606, '99001/cover_img.jpg', '{"Color":"Green"}', 2, 88);

INSERT INTO public.organization_shipping_service values('TEST', 99001, '{ "name": "Shop","type": "long","value": "14" }', 99001);

INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);


-- insert promotions
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630002, 'HI', 99001, now() - INTERVAL '2 DAY', now() + INTERVAL '2 DAY', 1, 0, 'GREEEEEED', '{"cart_amount_min":0, "amount_max":20000}', '{"amount":100.55}', 68, now());

INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630003, 'BYE', 99001, now() - INTERVAL '2 DAY', now() + INTERVAL '2 DAY', 1, 0, 'MORE_GREEEEEEED', '{"cart_amount_min":0, "amount_max":20000}', '{"percentage":10.99}', 68, now());

INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630004, 'BYE BYE', 99001, now() - INTERVAL '2 DAY', now() + INTERVAL '2 DAY', 1, 0, 'GREEEEEED_HEART', '{"cart_amount_min":0, "amount_max":20000}', '{"amount":100.55}', 68, now());

INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630005, 'SCAM', 99001, now() - INTERVAL '2 DAY', now() + INTERVAL '2 DAY', 1, 0, 'SCAM_GREEEEEEED', '{"cart_amount_min":10000 }', '{"percentage":10.99}', 68, now());

INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630006, 'DISAPPOINTMENT', 99001, now() - INTERVAL '2 DAY', now() + INTERVAL '2 DAY', 1, 0, 'kafa', '{"cart_amount_min":0, "discount_value_max":10 }', '{"percentage":10.99}', 68, now());



INSERT INTO public.meta_orders
(id, created_at, user_id, organization_id, status, sub_total, shipping_total, grand_total, discounts, notes, yeshtery_meta_order_id)
VALUES(13, now(), 88, 99001, 10, 1200.00, 25.50, 1124.95, 100.55, 'come after dinner', NULL);

INSERT INTO public.orders
(id, address, name, user_id, created_at, updated_at, date_delivery, organization_id, status, cancelation_reasons, shop_id, basket, sub_total, payment_status, payment_id, address_id, meta_order_id, total, discounts, promotion)
VALUES(1, NULL, 'alaa', 88, now(), now() , now() + INTERVAL '2 DAY', 99001, 10, '{}', 501, '{}', 1200.00, 0, NULL, 12300001, 13, 1225.50, 0.00, NULL);

INSERT INTO public.meta_orders_promotions
(promotion, meta_order)
VALUES(630002, 13);

INSERT INTO public.baskets
(id, order_id, stock_id, quantity, price, currency, discount, item_data, addon_price, special_order)
VALUES(1, 1, 601, 2.00, 600.00, 1, 0.00, '{"id":null,"product_id":1001,"name":"product_1","product_type":0,"stock_id":601,"brand_id":102,"brand_name":"brand_2","brand_logo":null,"variant_features":{},"quantity":2,"total_price":1200.00,"unit":"","thumb":null,"price":600.00,"discount":0.00,"variant_id":310001,"variant_name":"var","is_returnable":false,"currency_value":"EGP","sku":null,"product_code":null,"currency":"EGP","addon_total":0,"addons":[],"special_order":null,"p_name":null}', 0.00, NULL);