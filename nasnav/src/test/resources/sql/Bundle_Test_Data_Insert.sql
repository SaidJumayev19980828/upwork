
--dummy organization
insert into public.organizations(id , name )
values (99001, 'Bundle guys' );
insert into public.organizations(id , name )
values (99002, 'Other Bundle guys' );

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);


-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100001 , 'Bundle Shop'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100002 , 'another Shop'  , 99001);


-- dummy products
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200001, 'Bundle Product#1' , now() , now(), 0 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200002, 'Bundle Product#2' , now() , now(), 0 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200003, '#Bundle child' , now() , now() ,1 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200004, '#Bundle 4' , now() , now() ,1 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200005, '#Bundle 3' , now() , now() ,1 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200006, '#Bundle 2' , now() , now() ,1 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200007, '#Bundle 1' , now() , now() ,1 , 99001, 202);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200008, 'Empty Bundle' , now() , now() ,1 , 99001, 202);



-- variants for each product
insert into public.product_variants(id, "name" , product_id ) values(310001, 'var' 	, 200001);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 200002);
insert into public.product_variants(id, "name" , product_id ) values(310003, 'var' 	, 200003);
insert into public.product_variants(id, "name" , product_id ) values(310004, 'var' 	, 200004);
insert into public.product_variants(id, "name" , product_id ) values(310005, 'var' 	, 200005);
insert into public.product_variants(id, "name" , product_id ) values(310006, 'var' 	, 200006);
insert into public.product_variants(id, "name" , product_id ) values(310007, 'var' 	, 200007);
insert into public.product_variants(id, "name" , product_id ) values(310008, 'var' 	, 200008);




-- dummy variants for product 1 only
insert into public.product_variants(id, "name" , product_id ) values(300001, 'Green product' 	, 200001);
insert into public.product_variants(id, "name" , product_id ) values(300002, 'blue product' 	, 200001);
insert into public.product_variants(id, "name" , product_id ) values(300003, 'big product' 		, 200001);
insert into public.product_variants(id, "name" , product_id ) values(300004, 'small product' 	, 200001);


-- stocks for variants
insert into public.stocks(id, shop_id  , variant_id , quantity , price,  organization_id)
values (400001, 100001, 300001, 1 , 1000 , 99001);
insert into public.stocks(id, shop_id , variant_id , quantity , price,  organization_id)
values (400002, 100001, 300002, 20 , 122, 99001);
insert into public.stocks(id, shop_id  , variant_id , quantity , price,  organization_id)
values (400003, 100001, 300003, 20 , 3.5, 99001);
insert into public.stocks(id, shop_id  , variant_id , quantity , price,  organization_id)
values (400004, 100001, 300004, 20 , 88, 99001);

-- stocks for products and bundles(which are also products)
insert into public.stocks(id, shop_id, variant_id , quantity , price,  organization_id)
values (400005, 100001, 310002, 30 , 500, 99001);
insert into public.stocks(id, shop_id  , variant_id , quantity , price,  organization_id)
values (400006, 100001, 310003, 2000 , 3000, 99001);
insert into public.stocks(id, shop_id  , variant_id , quantity , price,  organization_id)
values (400007, 100001, 310004, 2000 , 10000, 99001);
insert into public.stocks(id, shop_id  , variant_id , quantity , price,  organization_id)
values (400008, 100001, 310007, 2000 , 10000, 99001);
insert into public.stocks(id, shop_id  , variant_id , quantity , price,  organization_id)
values (400009, 100001, 310005, 2000 , 10.10, 99001);
insert into public.stocks(id, shop_id  , variant_id , quantity , price,  organization_id)
values (400010, 100001, 310008, 101 , 100.10, 99001);
insert into public.stocks(id, shop_id  , variant_id , quantity , price,  organization_id)
values (400011, 100001, 310006, 2000 , 10000, 99001);



-- set child bundle items
insert into public.product_bundles(product_id, bundle_stock_id) values(200003 , 400005);

-- set main bundle , which contains product#1 2 variants and the child bundle
insert into public.product_bundles(product_id, bundle_stock_id) values(200004 , 400001);
insert into public.product_bundles(product_id, bundle_stock_id) values(200004 , 400002);
insert into public.product_bundles(product_id, bundle_stock_id) values(200004 , 400003);

-- other bundles
insert into public.product_bundles(product_id, bundle_stock_id) values(200005 , 400004);
insert into public.product_bundles(product_id, bundle_stock_id) values(200006 , 400006);
insert into public.product_bundles(product_id, bundle_stock_id) values(200007 , 400006);




--insering users
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  100001);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  100001);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser3@nasnav.com', 99002, '8874ssd', 100002);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (101, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (102, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (103, '8874ssd', now(), 70, null);


--inserting customers
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '123', now(), null, 88);


--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 2);



--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', null, 99001, 0);


--inserting orders
insert into public.orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(33, 88, now(), now(), 99001, 0, 501);

-- insert order items
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(33, 400008, 1, 10, 0);
INSERT INTO public.baskets(order_id, stock_id, quantity, price, currency)VALUES(33, 400004, 1, 10, 0);


commit;
