
--inserting organizations
INSERT INTO public.organizations(id, name) VALUES (99001, 'organization_1');
INSERT INTO public.organizations(id, name) VALUES (99002, 'organization_2');
INSERT INTO public.organizations(id, name) VALUES (99003, 'organization_2');


-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(502 , 'Bundle Shop'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(501 , 'another Shop'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(503 , 'Shop 3'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(504 , 'Shop 4'  , 99003);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(506 , 'Shop 5'  , 99002);


--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'user2@nasnav.com','user2','456', 99002);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (90, 'user3@nasnav.com','user3','789', 99003);


INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  503);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (71, 'testuser5@nasnav.com', 99003, '192021',  504);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (158, 'testuser3@nasnav.com', 99002, '222324',  506);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 5);


