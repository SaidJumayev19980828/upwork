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
INSERT INTO user_tokens (id, user_id, token) VALUES (1, 88, 'abc')