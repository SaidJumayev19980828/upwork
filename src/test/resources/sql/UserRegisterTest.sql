--inserting organizations
INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'organization-1');
INSERT INTO public.organizations(id, name,  p_name) VALUES (99002, 'organization_2', 'organization-2');

--inserting organization domain


-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(501 , 'Bundle Shop'  , 99002);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(502 , 'another Shop'  , 99001);

--inserting organizations domains
INSERT INTO public.organization_domains (id, "domain", subdir, organization_id) VALUES(55001, 'nasnav.com', 'organization_1', 99001);
INSERT INTO public.organization_domains (id, "domain", subdir, organization_id) VALUES(55002, 'nasnav.com', 'organization_2', 99002);
INSERT INTO public.organization_domains (id, "domain", organization_id) VALUES(55003, 'tooawsome.com', 99001);



--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password, user_status)
    VALUES (88001, 'user1@nasnav.com','user1','123', 99001, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru', 201);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password, user_status)
    VALUES (88002, 'user1@nasnav.com','user1','456', 99002, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru', 201);
INSERT INTO public.users(id, email,  user_name, authentication_token, phone_number, image, organization_id, encrypted_password, user_status)
    VALUES (88003, 'user2@nasnav.com','user2','789', '+021092154875','/urls/images/fdsafag23.jpg',  99001, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru', 201);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password, user_status, reset_password_token)
    VALUES (88004, 'not.activated@nasnav.com','not activated','951', 99001, '963', 200, 'sfdsdfd81');
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password, user_status,
    reset_password_token, reset_password_sent_at)
    VALUES (88005, 'no.token.man@nasnav.com','not activated but no token','77', 99001, '963', 200,
     'd67438ac-f3a5-4939-9686-a1fc096f3f4f', now());
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password, user_status, reset_password_token)
    VALUES (88006, 'suspended.man@nasnav.com','suspended','88', 99001, '963', 202, null);
   
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700001, '123', now(), null, 88001);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700002, '456', now(), null, 88002);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '789', now(), null, 88003);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700004, '951', now(), null, 88004);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700005, '77', now(), null, 88005);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700006, '88', now(), null, 88005);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700007, '99', now(), null, 88005);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700008, '889966', NOW() - INTERVAL '1 YEAR', null, 88005);


--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (159, 'Walid', 'user2@nasnav.com', 99001, 'nopqrst',  502, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (160, 'Walid 2', 'emp.user@nasnav.com', 99001, 't',  502, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id, encrypted_password)
VALUES (161, 'Walid 3', 'emp.user3@nasnav.com', 99001, 'tt',  502, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700009, '875488', NOW() - INTERVAL '29 DAY', 159, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700010, '101112', now(), 159, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700011, '131415', now(), 159, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700012, '161718', now(), 159, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700013, '192021', now(), 160, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700014, '222324', now(), 161, null);


insert into roles(id, name,  organization_id) values(1, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(3, 'ORGANIZATION_EMPLOYEE', 99001);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 159, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 160, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 161, 3);


INSERT INTO public.user_subscriptions VALUES (10001, 'sub@g.com', 99001, null);
INSERT INTO public.user_subscriptions VALUES (10002, 'seocnd_sub@g.com', 99002, null);



--inserting countries/ cities/ areas/ sub-areas
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id, country_id, name) values(100001, 1,'Cairo');
INSERT INTO public.areas(id, name, city_id) values(100001, 'new cairo', 100001);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888001, 100001, 'Badr city', 99001);
insert into public.sub_areas ("id",area_id, "name", organization_id) values (888002, 100001, 'Future city', 99002);


INSERT INTO public.addresses(id, address_line_1, sub_area_id, area_Id) values(12300003, 'address line', 888001, 100001);
INSERT INTO public.User_addresses(id, user_id ,address_id ,principal ) values(12300003, 88005, 12300003, false);