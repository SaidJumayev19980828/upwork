INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
-- inserting organizations
INSERT INTO public.organizations(id, name, currency_iso, yeshtery_state) VALUES (99001, 'organization_1', 818, 1);

-- inserting yeshtery users
INSERT INTO public.yeshtery_users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);

-- inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id, yeshtery_user_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001, 88);

-- inserting auth tokens
INSERT INTO user_tokens (id, user_id, token) VALUES (1, 88, 'abc');

--insert permissions
insert into permission(id, name) values(1, 'CHAT_VISITOR');
insert into permission(id, name) values(2, 'CHAT_AGENT');

--inserting package
INSERT INTO public.package(id,name,description,price,period_in_days,currency_iso,stripe_price_id) values (99001,'Basic','test description',1.5,30,818,'price_1NzLNBGR4qGEOW4EItZ5eADp');
--inserting service
INSERT INTO public.service(id,code,name,description) values (99001,'CHAT_SERVICES','CHAT_SERVICES','CHAT_SERVICES Service');

-- Join package & service
INSERT INTO public.package_service(package_id,service_id) values (99001,99001);
INSERT INTO public.employee_users(id, email, name, authentication_token, organization_id)
VALUES (65, 'employee1@nasnav.com', 'employee1', 'qwe', 99001);
INSERT INTO public.package_registered(id, creator_employee_id, org_id, package_id, registered_date) values (200, 65, 99001, 99001, now());
INSERT INTO public.organization_services(id, org_id, service_id, enabled) values (1, 99001, 99001, true);
INSERT INTO public.service_permissions(service_id, permission_id) values (99001, 1);
INSERT INTO public.service_permissions(service_id, permission_id) values (99001, 2);