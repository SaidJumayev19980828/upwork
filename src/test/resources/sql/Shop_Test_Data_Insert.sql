----------------------------deleting previous data----------------------------
delete from public.stocks where organization_id between 99000 and 99999;
delete from public.shops where organization_id between 99000 and 99999;
delete from public.role_employee_users WHERE employee_user_id IN (SELECT id FROM public.employee_users where organization_id between 99000 and 99999); 
delete from public.users where organization_id between 99000 and 99999;
delete from public.employee_users where organization_id between 99000 and 99999;
delete from public.roles;
delete from public.products where organization_id between 99000 and 99999;
delete from public.brands where organization_id between 99000 and 99999;
delete from public.organizations where id between 99000 and 99999;

----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name) VALUES (99001, 'organization_1');
INSERT INTO public.organizations(id, name) VALUES (99002, 'organization_2');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id) VALUES (501, 'shop_1', 102, 99002);
INSERT INTO public.shops(id, name, brand_id,  organization_id) VALUES (502, 'shop_2', 101, 99001);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);

INSERT INTO public.user_tokens(id, token, update_time, user_id) VALUES (1, '123', now(), 88);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (71, 'testuser5@nasnav.com', 99001, '192021',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (158, 'testuser3@nasnav.com', 99002, '222324',  501);

INSERT INTO public.emp_user_tokens(id, token, update_time, user_id) VALUES (1, '101112', now(), 68);
INSERT INTO public.emp_user_tokens(id, token, update_time, user_id) VALUES (2, '131415', now(), 69);
INSERT INTO public.emp_user_tokens(id, token, update_time, user_id) VALUES (3, '161718', now(), 70);
INSERT INTO public.emp_user_tokens(id, token, update_time, user_id) VALUES (4, '192021', now(), 71);
INSERT INTO public.emp_user_tokens(id, token, update_time, user_id) VALUES (5, '222324', now(), 158);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(3, 'STORE_EMPLOYEE', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 5);