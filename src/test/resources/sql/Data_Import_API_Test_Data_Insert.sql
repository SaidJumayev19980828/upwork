--dummy organization
insert into public.organizations(id , name , created_at, updated_at)
values (99001, 'Data guys' , now() , now());
insert into public.organizations(id , name , created_at, updated_at)
values (99002, 'Evil guys' , now() , now());

--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (201, 'squishy things', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (202, 'mountain equipment', now(), now());

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'squish', now(), now(), 99001);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'hardy', now(), now(), 99001);


-- dummy shop
INSERT INTO public.shops (id,"name", created_at , updated_at , organization_id) VALUES(100001 , 'Funny Shop' , now() , now() , 99001);
INSERT INTO public.shops (id,"name", created_at , updated_at , organization_id) VALUES(100002 , 'Wealthy Shop' , now() , now() , 99001);
INSERT INTO public.shops (id,"name", created_at , updated_at , organization_id) VALUES(100003 , 'Import Shop' , now() , now() , 99001);
INSERT INTO public.shops (id,"name", created_at , updated_at , organization_id) VALUES(100004 , 'Update Shop' , now() , now() , 99001);



--insert product features for the organziation
INSERT INTO public.product_features(id,"name", p_name, description, organization_id)
VALUES(7001,'size', 'size', 'test feature', 99001);
INSERT INTO public.product_features(id,"name", p_name, description, organization_id)
VALUES(7002,'color', 'color', 'test feature', 99001);
INSERT INTO public.product_features(id,"name", p_name, description, organization_id)
VALUES(7003,'color', 'color', 'test feature', 99002);



-- dummy products
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200001, 'Bundle Product#1' , now() , now(), 0 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200002, 'Bundle Product#2' , now() , now(), 0 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id, p_name, description, brand_id, barcode) 
VALUES(200003, 'Product to update' , now() , now(), 0 , 99001, 201, 'u_shoe', 'old desc', 101, 'TT232222');


-- variants for each product
insert into public.product_variants(id, "name" , product_id ,barcode, feature_spec) values(310001, 'var', 200001, '12345ABC', '{}');
insert into public.product_variants(id, "name" , product_id ,barcode, feature_spec) values(310002, 'var', 200002, '45678EFG', '{}');
insert into public.product_variants(id, "name" , product_id ,barcode, p_name, description, feature_spec) values(310003, 'Product to update', 200003, 'TT232222', 'u_shoe', 'old desc', '{}');


-- stocks for variants
insert into public.stocks(id, shop_id  , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400001, 100001, 310001, 1 , 1000 , now() , now(), 99001);
insert into public.stocks(id, shop_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400002, 100001,310002, 20 , 122, now() , now(), 99001);
insert into public.stocks(id, shop_id , variant_id , quantity , price, created_at, updated_at, organization_id, currency)
values (400003, 100004,310003, 30, 15, now() , now(), 99001, 2);



--insering users
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (68, now(), now(), 'testuser1@nasnav.com', 99001, '101112',  100001);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (69, now(), now(), 'testuser2@nasnav.com', 99001, '131415',  100001);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (70, now(), now(), 'testuser3@nasnav.com', 99002, '898dssd',  100002);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (71, now(), now(), 'testuser4@nasnav.com', 99001, 'ggr45r5',  100003);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (72, now(), now(), 'testuser5@nasnav.com', 99001, 'edddre2',  100004);


--inserting Roles
insert into public.roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(5, 'STORE_EMPLOYEE', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(3, 'CUSTOMER', now(), now(), 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 70, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (23, 71, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (24, 72, 2, now(), now());


