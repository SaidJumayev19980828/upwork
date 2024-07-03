INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');

--inserting Organization
INSERT INTO public.organizations(id, name, p_name, yeshtery_state) VALUES (99001, 'organization_1', 'organization-1', 1);

-- inserting Shop
INSERT INTO public.shops (id,"name", organization_id) VALUES(502 , 'another Shop'  , 99001);

-- inserting Employee User
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, gender)
VALUES (163, 'nasnav admin', 'nasnav.admin@nasnav.com', 99001, 'nasnav-admin-token', 'MALE');
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, gender)
VALUES (164, 'org employee', 'org.employee@nasnav.com', 99001, 'nasnav-employee-token', 'MALE');

--inserting yeshtery users
INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token,gender, organization_id)
VALUES (808, 'user1@nasnav.com','user1','nasnav-customer-token','FEMALE', 99001);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id ,yeshtery_user_id)
VALUES (88, 'user1@nasnav.com','user1','nasnav-customer-token', 99001, 808);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id)
VALUES (700015, 'nasnav-admin-token', now(), 163, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id)
VALUES (700016, 'nasnav-customer-token', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id)
VALUES (700017, 'nasnav-employee-token', now(), 164, null);


-- inserting Roles
insert into roles(id, name,  organization_id) values(1, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(3, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(4, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(5, 'CUSTOMER', 99001);

INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (24, 163, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (25, 164, 3);





