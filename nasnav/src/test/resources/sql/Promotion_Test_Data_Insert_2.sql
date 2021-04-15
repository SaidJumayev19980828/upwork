
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'fortune');
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting organization domain
INSERT INTO public.organization_domains (id, "domain", organization_id) VALUES(1, 'fortune.nasnav.com', 99001);
INSERT INTO public.organization_domains (id, "domain", organization_id) VALUES(2, 'fortune-egypt.com', 99001);
INSERT INTO public.organization_domains (id, "domain", organization_id, subdir) VALUES(3, 'nasnav.com', 99001, 'fortune');


--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');


--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 201, 'brand_3', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (104, 201, 'brand_4', 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 102, 99001, 0);


-- countries and addresses
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);

INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300001, 'address line', 1, '01111234567');
INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300002, 'address line', 1, '01111234567');



--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser3@nasnav.com', 99001, '123456',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '123456', now(), 70, null);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(3, 'STORE_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(4, 'CUSTOMER', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 3);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'test2@nasnav.com','user2','456', 99001);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700004, '456', now(), null, 89);

INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);


-- inserting shipping service
INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters, id)VALUES('TEST', 99001, '{"hotline":"19888"}', 11001);


-- inserting promotions
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630001, 'WELCOME', 99001, now() + INTERVAL '200 DAY', now() + INTERVAL '201 DAY', 1, 0, 'WELCOME2020', '{}', '{}', 69, now());
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630002, 'HI', 99001, now() - INTERVAL '2 DAY', now() + INTERVAL '2 DAY', 1, 0, 'GREEEEEED', '{"amount_min":0, "amount_max":20000}', '{"percentage":10}', 69, now());
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630003, 'GIVE_US_MONEY', 99001, now() - INTERVAL '10 min' , now() + INTERVAL '200 min', 1, 0, 'MONEY2020', '{"amount_min":0, "amount_max":500}', '{"percentage":10}', 69, now()); 
INSERT INTO public.promotions
(id, identifier, organization_id, date_start, date_end, status, user_restricted, code, constrains, discount, created_by, created_on)
VALUES(630004, 'MORE_MONEY', 99001, now() - INTERVAL '100 DAY', now() - INTERVAL '50 DAY', 0, 0, 'MORE2020', '{"amount_min":0, "amount_max":2000}', '{"percentage":10}', 69, now());



-- used codes
INSERT INTO public.promotions_codes_used
(promotion_id, user_id, "time")
VALUES(630002, 88, now() - INTERVAL '1 DAY');



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
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(601, 502, 6, 99002, 600.00, 310001, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(602, 501, 8, 99001, 1200.0, 310002, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(603, 501, 4, 99002, 200.00, 310003, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(604, 502, 6, 99001, 700.00, 310004, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(605, 502, 0, 99001, 700.00, 310009, 0);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(606, 502, 1, 99001, 700.00, 310010, 1);


-- insert cart
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(602, '99001/img2.jpg', '{"Color":"Blue"}', 2, 88);
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) VALUES(604, '99001/cover_img.jpg', '{"Color":"Yellow"}', 1, 88);



