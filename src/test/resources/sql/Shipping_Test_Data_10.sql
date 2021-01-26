INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(3,'Nigeria', 556, 'NGN');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 556);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 556);


--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');


INSERT INTO public.cities(id,country_id, "name") VALUES(1001, 3, 'ABA');
INSERT INTO public.cities(id,country_id, "name") VALUES(1002, 3, 'ABAKALIKI');

INSERT INTO public.areas(id, "name", city_id)VALUES(11, 'AGBARAGWU', 1001);
INSERT INTO public.areas(id, "name", city_id)VALUES(22, 'ABA', 1002);

INSERT INTO public.shipping_service(id)VALUES('CLICKNSHIP');

INSERT into shipping_areas values(11, 'CLICKNSHIP', '2335');
INSERT into shipping_areas values(22, 'CLICKNSHIP', '2331');

INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300001, 'address line', 11, '01111234567');
INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300002, 'address line', 22, '01111234567');


--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id) VALUES (501, 'shop_1', 102, 99001, 0, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id) VALUES (502, 'shop_2', 101, 99001, 0, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id) VALUES (503, 'shop_3', 101, 99001, 0, 12300001);


--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'test2@nasnav.com','user2','456', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700004, '456', now(), null, 89);

INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);


--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2',101, 201, 99002, now(), now(),'123456789');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',102, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',102, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6',102, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1008, 'product_8',102, 202, 99002, now(), now());

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

-- first shop has all items
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(601, 503, 6, 99001, 100.00, 310004, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(602, 502, 8, 99001, 50.0, 310002, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(603, 501, 6, 99001, 60.00, 310009, 1);




-- insert cart
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id)
VALUES(601, '99001/img2.jpg', '{"Color":"Blue"}', 3, 88);
--INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id)
--VALUES(602, '99001/cover_img.jpg', '{"Color":"Yellow"}', 3, 88);
--INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id)
--VALUES(603, '99001/cover_img.jpg', '{"Color":"Yellow"}', 3, 88);

INSERT INTO public.organization_shipping_service values('CLICKNSHIP', 99001, '{
        "SERVER_URL": "https://api.clicknship.com.ng",
        "USER_NAME": "cnsdemoapiacct",
        "PASSWORD": "ClickNShip$12345",
        "GRANT_TYPE": "password"
      }', 99001);
