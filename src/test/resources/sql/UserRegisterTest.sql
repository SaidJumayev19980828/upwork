--inserting organizations
INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'organization-1');
INSERT INTO public.organizations(id, name,  p_name) VALUES (99002, 'organization_2', 'organization-2');

-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(501 , 'Bundle Shop'  , 99002);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(502 , 'another Shop'  , 99001);

--inserting organizations domains
INSERT INTO public.organization_domains (id, "domain", subdir, organization_id) VALUES(55001, 'nasnav.com', 'organization_1', 99001);
INSERT INTO public.organization_domains (id, "domain", subdir, organization_id) VALUES(55002, 'nasnav.com', 'organization_2', 99002);



--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password, user_status)
    VALUES (88001, 'user1@nasnav.com','user1','123', 99001, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru', 201);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password, user_status)
    VALUES (88002, 'user1@nasnav.com','user1','456', 99002, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru', 201);
INSERT INTO public.users(id, email,  user_name, authentication_token, address, country, city, phone_number, image, organization_id, encrypted_password, user_status)
    VALUES (88003, 'user2@nasnav.com','user2','789', '21 jump street', 'Egypt', 'Cairo', '+021092154875','/urls/images/fdsafag23.jpg',  99001, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru', 201);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password, user_status, reset_password_token)
    VALUES (88004, 'not.activated@nasnav.com','not activated','951', 99001, '963', 200, 'sfdsdfd81');
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, encrypted_password, user_status, reset_password_token)
    VALUES (88005, 'no.token.man@nasnav.com','not activated but no token','77', 99001, '963', 200, null);
   
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '123', now(), null, 88001);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '456', now(), null, 88002);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '789', now(), null, 88003);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, '951', now(), null, 88004);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (5, '77', now(), null, 88005);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (6, '88', now(), null, 88005);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (7, '99', now(), null, 88005);

--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id, encrypted_password)
	VALUES (159, 'Walid', 'user2@nasnav.com', 99001, 'nopqrst',  502, '$2a$10$/Nf8G202WWrAzmZjIKNR8.VvonJt7DB/cIciQ3S3ym1tD.IgaT1ru');

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (8, '101112', now(), 159, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (9, '131415', now(), 159, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (10, '161718', now(), 159, null);


insert into roles(id, name,  organization_id) values(1, 'ORGANIZATION_ADMIN', 99001);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 159, 1);