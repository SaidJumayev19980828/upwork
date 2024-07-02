----------------------------inserting dummy data----------------------------
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting yeshtery users
INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token, organization_id)
VALUES (808, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token, organization_id)
VALUES (809, 'test2@nasnav.com','user2','456', 99002);
INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token, organization_id)
VALUES (810, 'test3@nasnav.com','user3','789', 99001);
INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token, organization_id)
VALUES (811, 'test4@nasnav.com','user4','258', 99002);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id ,yeshtery_user_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001,808);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id,yeshtery_user_id)
VALUES (89, 'test2@nasnav.com','user2','456', 99002,809);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id,yeshtery_user_id)
VALUES (90, 'test3@nasnav.com','user3','789', 99001,810);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id,yeshtery_user_id)
VALUES (91, 'test4@nasnav.com','user4','258', 99002,811);

-- inserting employees
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token)
	VALUES (67, 'employee1@nasnav.com', 99001, 'abc');

-- tokens
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700004, '456', now(), null, 89);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700005, '789', now(), null, 90);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700006, '258', now(), null, 91);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700007, 'abc', now(), 67, null);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'MEETUSVR_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(6, 'CUSTOMER', 99001);
insert into roles(id, name,  organization_id) values(7, 'MEETUSVR_ADMIN', 99002);
insert into roles(id, name,  organization_id) values(8, 'ORGANIZATION_ADMIN', 99002);
insert into roles(id, name,  organization_id) values(9, 'ORGANIZATION_EMPLOYEE', 99002);
insert into roles(id, name,  organization_id) values(10, 'STORE_EMPLOYEE', 99002);
insert into roles(id, name,  organization_id) values(11, 'CUSTOMER', 99002);

-- employee roles
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 67, 2);


--insert loyalty point transaction entity
INSERT INTO public.loyalty_point_transactions(id,user_id,points,org_id,start_date,type,amount)values(1,88,5,99001,now() - INTERVAL '2 DAY',2,10);
INSERT INTO public.loyalty_point_transactions(id,user_id,points,org_id,start_date,type,amount)values(2,88,15,99001,now() - INTERVAL '2 DAY',2,10);
INSERT INTO public.loyalty_point_transactions(id,user_id,points,org_id,start_date,type,amount)values(3,88,5,99001,now() - INTERVAL '2 DAY',110,30);
INSERT INTO public.loyalty_point_transactions(id,user_id,points,org_id,start_date,type,amount)values(4,89,30,99002,now() - INTERVAL '2 DAY',2,40);

--insert loyalty spent transaction entity
INSERT INTO public.loyalty_spent_transactions(id,transaction_id,reverse_transaction_id)values(1,1,3);
--INSERT INTO public.loyalty_spent_transactions(id,transaction_id,reverse_transaction_id)values(2,2,4);

--insert settings
INSERT INTO public.settings(id, setting_name, setting_value, organization_id, type)
VALUES (1, 'RETURN_DAYS_LIMIT', '0', 99001, 0);
INSERT INTO public.settings(id, setting_name, setting_value, organization_id, type)
VALUES (99002, 'RETURN_DAYS_LIMIT', '0', 99002, 0);


INSERT INTO public.user_loyalty_points(
	id, balance, created_at, version, user_id)
	VALUES
	 (1, 200, now(), 1, 88);