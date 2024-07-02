INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');

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
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100011 , 'another Shop - same org'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100012 , 'another another Shop - same org'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100002 , 'other org Shop'  , 99002);


-- dummy products
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200001, 'Bundle Product#1' , now() , now(), 0 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200002, 'Bundle Product#2' , now() , now(), 0 , 99001, 201);



-- variants for each product
insert into public.product_variants(id, "name" , product_id ) values(310001, 'var' 	, 200001);
insert into public.product_variants(id, "name" , product_id ) values(310011, 'var' 	, 200001);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 200002);



-- existing stocks for variants
insert into public.stocks(id, shop_id  , variant_id , quantity , price,  organization_id)
values (400001, 100001, 310011, 1 , 1000 , 99001);




--insering users
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  100001);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  100001);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser3@nasnav.com', 99002, '8874ssd',  100002);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (71, 'testuser4@nasnav.com', 99001, 'sfsd885d',  100011);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (72, 'testuser5@nasnav.com', 99001, 'esdffeded',  100012);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '8874ssd', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, 'sfsd885d', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (5, 'esdffeded', now(), 72, null);



--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'MEETUSVR_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);
insert into public.roles(id, name,  organization_id) values(6, 'ORGANIZATION_MANAGER', 99001);
insert into public.roles(id, name,  organization_id) values(7, 'STORE_MANAGER', 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 7);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (24, 72, 6);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (25, 72, 7);

