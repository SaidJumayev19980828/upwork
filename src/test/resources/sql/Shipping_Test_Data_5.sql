----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name) VALUES (99001, 'organization_1');
INSERT INTO public.organizations(id, name) VALUES (99002, 'organization_2');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Shoe size', 's-size', 'Size of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Shoe color', 's-color', 'Color of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Shoe size', 's-size', 'Size of the shoes', 99002);

INSERT INTO public.countries(id,"name")VALUES(1,'Egypt');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.cities(id,country_id, "name") VALUES(2,1, 'Alexandria');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
INSERT INTO public.areas(id, "name", city_id)VALUES(2, 'Mokatem', 1);
INSERT INTO public.areas(id, "name", city_id)VALUES(3, 'Miami', 2);

INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300001, 'address line', 1, '01111234567');
INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300002, 'address line', 2, '01111234567');
INSERT INTO public.addresses(id, address_line_1, area_id, phone_number) values(12300003, 'shop address', 3, '01111234567');


--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (501, 'shop_1', 102, 99001, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (502, 'shop_2', 101, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (503, 'shop_3', 101, 99001, 12300003);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (504, 'shop_4', 101, 99001, 12300002);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (505, 'shop_5', 101, 99001, 12300002);

--insering employees
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'ahmed.galal@nasnav.com', 99001, '161718',  502);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700001, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700002, '131415', now(), 69, null);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'test2@nasnav.com','user2','456', 99001);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700004, '456', now(), null, 89);



INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 68, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 69, 2);


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
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(601, 503, 6, 99001, 700.00, 310004, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(602, 503, 8, 99001, 1200.0, 310002, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(603, 503, 6, 99001, 800.00, 310009, 1);

-- second shop
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(604, 501, 5, 99001, 700.00, 310004, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(605, 501, 5, 99001, 1200.0, 310002, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(606, 501, 5, 99001, 800.00, 310009, 1);


-- thrid shop has enough quantity of only two items
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(607, 502, 8, 99001, 700.00, 310004, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(608, 502, 8, 99001, 1200.0, 310002, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(609, 502, 1, 99001, 800.00, 310009, 1);


-- forth shop has only two items
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(610, 504, 7, 99001, 700.00, 310004, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(611, 504, 6, 99001, 1200.0, 310002, 1);


-- fifth has all items but is not in the allowed shop list
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(612, 505, 5, 99001, 700.00, 310004, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(613, 505, 5, 99001, 1200.0, 310002, 1);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(614, 505, 5, 99001, 800.00, 310009, 1);



-- insert cart
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) 
VALUES(612, '99001/img2.jpg', '{"Color":"Blue"}', 3, 88);
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) 
VALUES(613, '99001/cover_img.jpg', '{"Color":"Yellow"}', 3, 88);
INSERT INTO public.cart_items (stock_id, cover_image, variant_features, quantity, user_id) 
VALUES(614, '99001/cover_img.jpg', '{"Color":"Yellow"}', 3, 88);

INSERT INTO public.organization_shipping_service values('PICKUP', 99001, '{ "ALLOWED_SHOP_ID_LIST":[503,501] }', 99001);
