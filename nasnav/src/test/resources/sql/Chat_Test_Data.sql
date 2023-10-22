INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
-- inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);

-- inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);

-- inserting employees
INSERT INTO public.employee_users(id, email, name, authentication_token, organization_id)
    VALUES (65, 'employee1@nasnav.com', 'employee1', 'qwe', 99001);

-- inserting auth tokens
INSERT INTO public.user_tokens (id, user_id, token) VALUES (1, 88, 'abc');
INSERT INTO public.user_tokens (id, employee_user_id, token) VALUES (2, 65, 'qwe');

--inserting Roles
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 65, 2);