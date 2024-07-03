
----------------------------inserting dummy data----------------------------

insert into public.countries(id, "name", iso_code, currency)values(1,'Egypt', 818, 'EGP');

-- insert addresses
INSERT INTO public.cities(id, country_id, name) VALUES (1, 1, 'city_1');

INSERT INTO public.areas(id, name, city_id) VALUES (800, 'area_1', 1);

insert into public.addresses(id, address_line_1, area_id) values(12300001, 'address line', 800);


--inserting organizations
insert into public.organizations(id, name, currency_iso, yeshtery_state) values (99001, 'organization_1', 818, 1);
insert into public.organizations(id, name, currency_iso, yeshtery_state) values (99002, 'organization_2', 818, 1);
insert into public.organizations(id, name, p_name, yeshtery_state) values (99003, 'yeshtery', 'yeshtery', 1);

--inserting brands
insert into public.brands(id, category_id, name, organization_id) values (101, 202, 'brand_1', 99002);
insert into public.brands(id, category_id, name, organization_id) values (102, 201, 'brand_2', 99001);
insert into public.brands(id, category_id, name, organization_id) values (103, 202, 'brand_3', 99001);

--inserting categories
insert into public.categories(id, name) values (201, 'category_1');
insert into public.categories(id, name) values (202, 'category_2');

--inserting shops
insert into public.shops(id, name, brand_id,  organization_id, removed, address_id) values (501, 'shop_1', 102, 99001, 0, 12300001);
insert into public.shops(id, name, brand_id,  organization_id, removed, address_id) values (502, 'shop_2', 101, 99001, 0, 12300001);
insert into public.shops(id, name, brand_id,  organization_id, removed, address_id) values (503, 'shop_3', 102, 99001, 0, 12300001);
insert into public.shops(id, name, brand_id,  organization_id, removed, address_id) values (504, 'shop_4', 103, 99001, 0, 12300001);
insert into public.shops(id, name, brand_id,  organization_id, removed, address_id) values (505, 'shop_5', 101, 99001, 0, 12300001);
insert into public.shops(id, name, brand_id,  organization_id, removed, address_id) values (506, 'shop_6', 102, 99002, 0, 12300001);

--inserting users
insert into public.users(id, email, phone_number,  user_name, authentication_token, organization_id)
values (88, 'user1@nasnav.com', '01234567891', 'user1','123', 99001);
insert into public.users(id, email, phone_number,  user_name, authentication_token, organization_id)
values (89, 'user2@nasnav.com', '01234567892', 'user2','456', 99002);
insert into public.users(id, email, phone_number,  user_name, authentication_token, organization_id)
values (90, 'user3@nasnav.com', '01234567893', 'user3','789', 99001);

insert into public.user_tokens(id, token, update_time, employee_user_id, user_id) values (101, '123', now(), null, 88);
insert into public.user_tokens(id, token, update_time, employee_user_id, user_id) values (102, '456', now(), null,89);


-- insert user addresses
insert into public.User_addresses values(120, 88, 12300001, false);


--insert employee users
insert into public.employee_users(id,  email, organization_id, authentication_token, shop_id)
values (68, 'testuser1@nasnav.com', 99001, '101112',  502);
insert into public.employee_users(id,  email, organization_id, authentication_token, shop_id)
values (69, 'testuser2@nasnav.com', 99001, '131415',  503);
insert into public.employee_users(id,  email, organization_id, authentication_token, shop_id)
values (70, 'testuser4@nasnav.com', 99001, '161718',  503);
insert into public.employee_users(id,  email, organization_id, authentication_token, shop_id)
values (71, 'testuser5@nasnav.com', 99003, '192021',  504);
insert into public.employee_users(id,  email, organization_id, authentication_token, shop_id)
values (72, 'testuser6@nasnav.com', 99001, 'sdrf8s',  501);
insert into public.employee_users(id,  email, organization_id, authentication_token, shop_id)
values (73, 'testuser7@nasnav.com', 99001, 'sdfe47',  502);
insert into public.employee_users(id,  email, organization_id, authentication_token, shop_id)
values (158, 'testuser3@nasnav.com', 99002, '222324',  506);

insert into public.user_tokens(id, token, update_time, employee_user_id ,user_id) values (103, '101112', now(), 68, null);
insert into public.user_tokens(id, token, update_time, employee_user_id ,user_id) values (104, '131415', now(), 69, null);
insert into public.user_tokens(id, token, update_time, employee_user_id ,user_id) values (105, '161718', now(), 70, null);
insert into public.user_tokens(id, token, update_time, employee_user_id ,user_id) values (106, '192021', now(), 71, null);
insert into public.user_tokens(id, token, update_time, employee_user_id ,user_id) values (107, 'sdrf8s', now(), 72, null);
insert into public.user_tokens(id, token, update_time, employee_user_id ,user_id) values (108, 'sdfe47', now(), 73, null);
insert into public.user_tokens(id, token, update_time, employee_user_id ,user_id) values (109, '222324', now(), 158, null);
--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);
insert into public.roles(id, name,  organization_id) values(6, 'STORE_MANAGER', 99001);
insert into public.roles(id, name,  organization_id) values(7, 'ORGANIZATION_MANAGER', 99001);


--inserting Roles EmployeeUsers relations
insert into public.role_employee_users(id, employee_user_id, role_id) values (20, 68, 1);
insert into public.role_employee_users(id, employee_user_id, role_id) values (21, 69, 2);
insert into public.role_employee_users(id, employee_user_id, role_id) values (23, 69, 7);
insert into public.role_employee_users(id, employee_user_id, role_id) values (24, 70, 4);
insert into public.role_employee_users(id, employee_user_id, role_id) values (28, 70, 7);
insert into public.role_employee_users(id, employee_user_id, role_id) values (25, 71, 5);
insert into public.role_employee_users(id, employee_user_id, role_id) values (26, 72, 6);
insert into public.role_employee_users(id, employee_user_id, role_id) values (27, 73, 6);




--inserting products
insert into public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) values (1001, 'product_1',101, 201, 99001, now(), now());
insert into public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) values (1002, 'product_2',101, 201, 99001, now(), now());
insert into public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) values (1003, 'product_3',101, 201, 99001, now(), now());
insert into public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) values (1004, 'product_4',101, 201, 99001, now(), now());
insert into public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) values (1005, 'product_5',101, 201, 99002, now(), now());

-- variants for each product
insert into public.product_variants(id, "name" , product_id ) values(310001, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1002);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1003);
insert into public.product_variants(id, "name" , product_id ) values(310004, 'var' 	, 1004);
insert into public.product_variants(id, "name" , product_id ) values(310005, 'var' 	, 1004);
insert into public.product_variants(id, "name" , product_id ) values(310006, 'var' 	, 1005);


-- inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 501, 20, 99001, 60.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 502, 10, 99001, 70.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 502, 5, 99001, 0.0, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 503, 5, 99001, 0.0, 310004);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(605, 505, 5, 99001, 0.0, 310005);

--insert shipping service
INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters)
values('BOSTA_LEVIS', 99001,
'{
    "AUTH_TOKEN": "ae5d5b5601fb68f1b26bf1f059ecaa1a5f9963675707879f2d8a9b0ccfb00357",
    "BUSINESS_ID": "yM1ngytZ0",
    "SERVER_URL": "https://stg-app.bosta.co/api/v0",
	"WEBHOOK_URL": "https://backend.nasnav.org/callbacks/shipping/service/BOSTA_LEVIS/99001",
	"CAIRO_PRICE": 25,
	"ALEXANDRIA_PRICE":30,
	"DELTA_CANAL_PRICE":30,
	"UPPER_EGYPT":45,
	"TRACKING_URL": "https://stg-app.bosta.co/api/tracking/26522"
 }');

insert into public.meta_orders(id, created_at, user_id, organization_id, status) values(310001 , now(), 88, 99003, 8);
insert into public.meta_orders(id, created_at, user_id, organization_id, status, yeshtery_meta_order_id) values(310002 , now(), 89, 99001, 8, 310001);
insert into public.meta_orders(id, created_at, user_id, organization_id, status, yeshtery_meta_order_id) values(310003 , now(), 88, 99001, 8, 310001);

insert into public.orders(id, user_id, created_at, updated_at, organization_id, status, shop_id, meta_order_id, address_id)
values(5001, 88, now(), now(), 99001, 8, 501, 310002, 12300001);
insert into public.orders(id, user_id, created_at, updated_at, organization_id, status,shop_id, meta_order_id, address_id)
values(5002, 88, now(), now(), 99001, 8, 502, 310002, 12300001);
insert into public.orders(id, user_id, created_at, updated_at, organization_id, status,shop_id, meta_order_id, address_id)
values(5003, 88, now(), now(), 99001, 8, 503, 310003, 12300001);

-- insert shipments
insert into shipment (id, sub_order_id, shipping_service_id, created_at) values (2001, 5001, 'BOSTA_LEVIS', now());
insert into shipment (id, sub_order_id, shipping_service_id, created_at) values (2002, 5002, 'BOSTA_LEVIS', now());
insert into shipment (id, sub_order_id, shipping_service_id, created_at) values (2003, 5003, 'BOSTA_LEVIS', now());

-- insert order items
insert into public.baskets(id, order_id, stock_id, quantity, price, currency)values(440031, 5001, 601, 1, 60.0, 1);
insert into public.baskets(id, order_id, stock_id, quantity, price, currency)values(440032, 5001, 605, 2, 70.0, 1);
insert into public.baskets(id, order_id, stock_id, quantity, price, currency)values(440033, 5002, 604, 3, 80.0, 1);
insert into public.baskets(id, order_id, stock_id, quantity, price, currency)values(440034, 5003, 603, 3, 90.0, 1);
