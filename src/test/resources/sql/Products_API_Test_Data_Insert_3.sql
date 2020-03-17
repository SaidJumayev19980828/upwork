----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99002);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);

--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (201, 'category_1', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (202, 'category_2', now(), now());

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 99002);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 99001);

--inserting users
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id,country,city ,address)
    VALUES (88, 'user1@nasnav.com',now(), now(), 'user1','123', 99002, 'Egypt', 'Cairo', '12 Abbas el-Akkad, Nasr City');


--insering employee users
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (68, now(), now(), 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (69, now(), now(), 'testuser2@nasnav.com', 99002, '131415',  501);


--inserting Roles
insert into public.roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(5, 'STORE_EMPLOYEE', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(3, 'CUSTOMER', now(), now(), 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());



--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2',101, 201, 99002, now(), now(),'123456789');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',102, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',102, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6',102, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1008, 'product_8',102, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1012, 'product_12',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1013, 'product_13',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1014, 'product_14',101, 202, 99002, now(), now());


-- variants for each product
insert into public.product_variants(id, "name" , product_id ) values(310001, 'var' 	, 1001);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1002);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 1003);
insert into public.product_variants(id, "name" , product_id ) values(310004, 'var' 	, 1004);
insert into public.product_variants(id, "name" , product_id ) values(310005, 'var' 	, 1005);
insert into public.product_variants(id, "name" , product_id ) values(310006, 'var' 	, 1006);
insert into public.product_variants(id, "name" , product_id ) values(310007, 'var' 	, 1007);
insert into public.product_variants(id, "name" , product_id ) values(310008, 'var' 	, 1008);
insert into public.product_variants(id, "name" , product_id ) values(310012, 'var' 	, 1012);
insert into public.product_variants(id, "name" , product_id ) values(310013, 'var' 	, 1013);
insert into public.product_variants(id, "name" , product_id ) values(310014, 'var' 	, 1014);


--inserting stocks
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(601, 502, 6, now(), now(), 99002, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(602, 501, 8, now(), now(), 99001, 1200.0, 310002);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(603, 501, 4, now(), now(), 99002, 200.0, 310003);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(604, 502, 6, now(), now(), 99001, 700.0, 310004);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(605, 502, 6, now(), now(), 99001, 700.0, 310005);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(606, 502, 6, now(), now(), 99001, 700.0, 310006);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(612, 502, 6, now(), now(), 99001, 700.0, 310012);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(613, 502, 6, now(), now(), 99002, 800.0, 310013);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(614, 502, 6, now(), now(), 99002, 600.0, 310014);


--insert bundle 
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, product_type) VALUES (1009, 'bundle',102, 202, 99002, now(), now(),1);
insert into public.product_variants(id, "name" , product_id ) values(310009, 'var' 	, 1009);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(607, 502, 6, now(), now(), 99002, 600.0, 310009);

INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, product_type) VALUES (1010, 'bundle_to_delete',102, 202, 99002, now(), now(),1);
insert into public.product_variants(id, "name" , product_id ) values(310010, 'var' 	, 1010);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(608, 502, 6, now(), now(), 99002, 600.0, 310010);

INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, product_type, removed) VALUES (1011, 'deleted_bundle',102, 202, 99002, now(), now(),1, 1);
insert into public.product_variants(id, "name" , product_id ) values(310011, 'var' 	, 1011);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, variant_id) values(609, 502, 6, now(), now(), 99002, 600.0, 310011);


-- set child bundle items
insert into public.product_bundles(product_id, bundle_stock_id)
values(1009 , 603);
insert into public.product_bundles(product_id, bundle_stock_id)
values(1010 , 603);
insert into public.product_bundles(product_id, bundle_stock_id)
values(1011 , 612);


-- insert image for products  which should be deleted in tests
INSERT INTO public.files(organization_id, url, location)
VALUES(99001, 'img1.jpg', 'img1.jpg');
INSERT INTO public.files(organization_id, url, location)
VALUES(99001, 'img2.jpg', 'img2.jpg');
INSERT INTO public.files(organization_id, url, location)
VALUES(99001, 'img3.jpg', 'img3.jpg');

INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)
VALUES(1008, null, 0, 1, 'img1.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)
VALUES(1003, null, 0, 1, 'img2.jpg');
INSERT INTO public.product_images(product_id, variant_id, "type", priority, uri)
VALUES(1010, null, 0, 1, 'img3.jpg');


--inserting orders
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(630001, 88, now(), now(), 99001, 0, 502);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(630013, 88, now(), now(), 99002, 0, 502);
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(630014, 88, now(), now(), 99002, 1, 502);



-- insert order items
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(630001, 601, 14, 800.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(630013, 613, 14, 600.0, 1);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(630014, 614, 14, 600.0, 1);



insert into Tags(id, name, alias, category_id, organization_id, metadata) values(5001, 'tag_1', 'tag_1', 201, 99001, '');
insert into Tags(id, name, alias, category_id, organization_id, metadata) values(5002, 'tag_2', 'tag_2', 201, 99002, '');
insert into Tags(id, name, alias, category_id, organization_id, metadata) values(5003, 'tag_3', 'tag_2', 201, 99002, '');

insert into product_tags(product_id, tag_id) values(1001, 5001);
insert into product_tags(product_id, tag_id) values(1005, 5001);
