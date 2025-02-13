----------------------------inserting dummy data----------------------------

INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);
INSERT INTO public.organizations(id, name, p_name, currency_iso) VALUES (99003, 'yeshtery', 'yeshtery', 818);

--insert organization domain
INSERT INTO public.organization_domains
("domain", organization_id, subdir)
VALUES('develop.nasnav.org', 99001, null);


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


INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888001, 1, 'Badr city', 99001);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888002, 1, 'Badr city', 99002);



INSERT INTO public.addresses(id, address_line_1, area_id, phone_number, sub_area_id) values(12300001, 'address line', 1, '+1111234567', 888001);
INSERT INTO public.addresses(id, address_line_1, area_id, phone_number, sub_area_id) values(12300002, 'address line', 1, '+1111234567', 888001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (501, 'shop_1', 102, 99001, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (502, 'shop_2', 101, 99001, 12300001);

--insering employees
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'levis.nasnav@gmail.com', 99001, '131415',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'ahmed.galal@nasnav.com', 99001, '161718',  502);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700001, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700002, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '161718', now(), 70, null);

--inserting yeshtery users
INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token, organization_id)
    VALUES (808, 'user1@nasnav.com','user1','123', 99001);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, yeshtery_user_id)
    VALUES (88, 'test2@nasnav.com','user1','123', 99001, 808);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, yeshtery_user_id)
    VALUES (89, 'test4@nasnav.com','user2','456', 99002, 808);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, yeshtery_user_id)
    VALUES (90, 'test4@nasnav.com','user2','789', 99003, 808);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700005, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700006, '456', now(), null, 89);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700007, '789', now(), null, 90);


--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(3, 'ORGANIZATION_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(6, 'CUSTOMER', 99001);
insert into roles(id, name,  organization_id) values(7, 'STORE_MANAGER', 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 68, 3);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 69, 3);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 70, 7);


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
insert into public.product_variants(id, "name" , product_id, product_code, sku ) values(310002, 'var' 	, 1001, 'ABC123', '99999');
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id, feature_spec , product_code, sku) values(310004, 'var' 	, 1004, '{"234":"45"
}', null, '122558');
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


--INSERT dummy images
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/img1.jpg', '/dummy_loc1.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/img2.jpg', '/dummy_loc2.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/img3.jpg', '/dummy_loc3.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/cover_img.jpg', '/dummy_loc4.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/cover_img2.jpg', '/dummy_loc5.jpg', 'image/jpeg', 'dummy_loc.jpg');
INSERT INTO public.files(organization_id, url, "location", mimetype, orig_filename)VALUES(99001, '99001/cover_img3.jpg', '/dummy_loc6.jpg', 'image/jpeg', 'dummy_loc.jpg');


INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, 310001, 7, 0, '99001/img1.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, 310002, 7, 1, '99001/img2.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, null, 7, 1, '99001/img3.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, null, 7, 0, '99001/cover_img.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1001, null, 7, 0, '99001/cover_img2.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)VALUES(1002, null, 7, 0, '99001/cover_img3.jpg');


--INSERT INTO public.organization_shipping_service values('TEST', 99001, '{ "name": "Shop","type": "long","value": "14" }', 99001);
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
INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters)
values('BOSTA_LEVIS', 99002,
'{
    "AUTH_TOKEN": "ae5d5b5601fb68f1b26bf1f059ecaa1a5f9963675707879f2d8a9b0ccfb00357",
    "BUSINESS_ID": "yM1ngytZ0",
    "SERVER_URL": "https://stg-app.bosta.co/api/v0",
	"WEBHOOK_URL": "https://backend.nasnav.org/callbacks/shipping/service/BOSTA_LEVIS/99002",
	"CAIRO_PRICE": 25,
	"ALEXANDRIA_PRICE":30,
	"DELTA_CANAL_PRICE":30,
	"UPPER_EGYPT":45,
	"TRACKING_URL": "https://stg-app.bosta.co/api/tracking/26522"
 }');
INSERT INTO public.organization_shipping_service(shipping_service_id, organization_id, service_parameters)
values('BOSTA_LEVIS', 99003,
'{
    "AUTH_TOKEN": "ae5d5b5601fb68f1b26bf1f059ecaa1a5f9963675707879f2d8a9b0ccfb00357",
    "BUSINESS_ID": "yM1ngytZ0",
    "SERVER_URL": "https://stg-app.bosta.co/api/v0",
	"WEBHOOK_URL": "https://backend.nasnav.org/callbacks/shipping/service/BOSTA_LEVIS/99003",
	"CAIRO_PRICE": 25,
	"ALEXANDRIA_PRICE":30,
	"DELTA_CANAL_PRICE":30,
	"UPPER_EGYPT":45,
	"TRACKING_URL": "https://stg-app.bosta.co/api/tracking/26522"
 }');

--assign organization to cod payment
INSERT into public.organization_payments(id,organization_id ,gateway, account )
values(100025, 99001, 'cod', 'dummy');

INSERT INTO public.User_addresses values(12300001, 88, 12300001, false);
INSERT INTO public.User_addresses values(12300002, 89, 12300001, false);
INSERT INTO public.User_addresses values(12300003, 90, 12300001, false);