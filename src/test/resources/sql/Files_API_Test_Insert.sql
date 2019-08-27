
--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99003, 'organization_2', now(), now());

--inserting users
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com',now(), now(), 'user1','123', 99001);
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (89, 'user2@nasnav.com',now(), now(), 'user2','456', 99002);
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (90, 'user3@nasnav.com',now(), now(), 'user3','789', 99003);


INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (68, now(), now(), 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (70, now(), now(), 'testuser4@nasnav.com', 99001, '161718',  503);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (71, now(), now(), 'testuser5@nasnav.com', 99003, '192021',  504);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (158, now(), now(), 'testuser3@nasnav.com', 99002, '222324',  506);

--inserting Roles
insert into roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(5, 'STORE_EMPLOYEE', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(3, 'CUSTOMER', now(), now(), 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 70, 4, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (23, 71, 5, now(), now());


