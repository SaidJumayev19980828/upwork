--dummy organization
insert into public.organizations(id , name , created_at, updated_at)
values (99001, 'Bundle guys' , now() , now());
insert into public.organizations(id , name , created_at, updated_at)
values (99002, 'Other Bundle guys' , now() , now());

--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (201, 'category_1', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (202, 'category_2', now(), now());

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99001);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);


-- dummy shop
INSERT INTO public.shops (id,"name", created_at , updated_at , organization_id) VALUES(100001 , 'Bundle Shop' , now() , now() , 99001);
INSERT INTO public.shops (id,"name", created_at , updated_at , organization_id) VALUES(100011 , 'another Shop - same org' , now() , now() , 99001);
INSERT INTO public.shops (id,"name", created_at , updated_at , organization_id) VALUES(100012 , 'another Shop - same org' , now() , now() , 99001);
INSERT INTO public.shops (id,"name", created_at , updated_at , organization_id) VALUES(100002 , 'other org Shop' , now() , now() , 99002);


-- dummy products
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200001, 'Bundle Product#1' , now() , now(), 0 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200002, 'Bundle Product#2' , now() , now(), 0 , 99001, 201);



-- variants for each product
insert into public.product_variants(id, "name" , product_id ) values(310001, 'var' 	, 200001);
insert into public.product_variants(id, "name" , product_id ) values(310011, 'var' 	, 200001);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 200002);



-- existing stocks for variants
insert into public.stocks(id, shop_id  , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400001, 100001, 310011, 1 , 1000 , now() , now(), 99001);




--insering users
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (68, now(), now(), 'testuser1@nasnav.com', 99001, '101112',  100001);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (69, now(), now(), 'testuser2@nasnav.com', 99001, '131415',  100001);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (70, now(), now(), 'testuser3@nasnav.com', 99002, '8874ssd',  100002);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (71, now(), now(), 'testuser4@nasnav.com', 99001, 'sfsd885d',  100011);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
VALUES (72, now(), now(), 'testuser5@nasnav.com', 99001, 'esdffeded',  100012);




--inserting Roles
insert into public.roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(5, 'STORE_EMPLOYEE', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(3, 'CUSTOMER', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(6, 'ORGANIZATION_MANAGER', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(7, 'STORE_MANAGER', now(), now(), 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 6, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 70, 6, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (23, 71, 7, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (24, 72, 6, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (25, 72, 7, now(), now());

