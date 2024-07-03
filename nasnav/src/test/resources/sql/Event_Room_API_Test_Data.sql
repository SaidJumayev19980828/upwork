INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
INSERT INTO public.addresses(id, address_line_1, phone_number, area_id) values(12300001, 'Ali papa cave', 0, 1);

INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);


INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 201, 'brand_1', 99001);

INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id, code, allow_other_points) VALUES (501, 'shop_1', 101, 99001, 0, 12300001, 'code1', false);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id, code, allow_other_points) VALUES (502, 'shop_2', 101, 99001, 0, 12300001, 'code2', false);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id, code, allow_other_points) VALUES (503, 'shop_3', 101, 99001, 0, 12300001, 'code3', false);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed, address_id, code, allow_other_points) VALUES (504, 'shop_4', 101, 99002, 0, 12300001, 'code4', false);

INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (81, 'user81@nasnav.com','user1','user81', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (82, 'user82@nasnav.com','user2','user82', 99002);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (83, 'user83@nasnav.com','user3','user83', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (6, 'user81', now(), null, 81);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (7, 'user82', now(), null, 82);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (8, 'user83', now(), null, 83);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token)
	VALUES (68, 'testuser1@nasnav.com', 99001, '101112');
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token)
	VALUES (69, 'testuser2@nasnav.com', 99001, '131415');
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token)
	VALUES (70, 'testuser4@nasnav.com', 99002, '161718');
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token)
	VALUES (71, 'testuser5@nasnav.com', 99001, '192021');
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token)
	VALUES (158, 'testuser3@nasnav.com', 99001, '222324');

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '131415', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '161718', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (4, '192021', now(), 71, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (5, '222324', now(), 158, null);



--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99002);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99002);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_MANAGER', 99002);
insert into roles(id, name,  organization_id) values(5, 'STORE_MANAGER', 99002);
insert into roles(id, name,  organization_id) values(3, 'STORE_EMPLOYEE', 99002);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 5);

INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());

INSERT INTO public.influencers(id,created_at,user_id,approved) values (100,now(),81,true);
INSERT INTO public.influencers(id,created_at,user_id,approved) values (101,now(),83,true);

INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(51,now(),CURRENT_DATE + INTERVAL '1 day',CURRENT_DATE + INTERVAL '2 day',99001,false,'name100','desc',0);
INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(52,now(),CURRENT_DATE + INTERVAL '1 day',CURRENT_DATE + INTERVAL '2 day',99001,false,'name101','desc',0);
INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(53,now(),CURRENT_DATE + INTERVAL '1 day',CURRENT_DATE + INTERVAL '2 day',99002	,false,'name101','desc',0);
INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(54,now(),CURRENT_DATE - INTERVAL '5 day',CURRENT_DATE - INTERVAL '2 day',99001,false,'name100','desc',0);
INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(55,now(),CURRENT_DATE + INTERVAL '1 day',CURRENT_DATE + INTERVAL '2 day',99001,false,'name101','desc',0);
INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(56,now(),CURRENT_DATE + INTERVAL '1 day',CURRENT_DATE + INTERVAL '2 day',99001,false,'name101','desc',0);


INSERT INTO public.event_influencers(id, event_id, influencer_id)VALUES (1, 51, 100);
INSERT INTO public.event_influencers(id, event_id, influencer_id)VALUES (2, 52, 101);
INSERT INTO public.event_influencers(id, event_id, influencer_id)VALUES (3, 54, 100 );
INSERT INTO public.event_influencers(id, event_id, influencer_id)VALUES (4, 55 , 101);
INSERT INTO public.event_influencers(id, event_id, influencer_id)VALUES (5, 56, 101);

INSERT INTO public.event_requests(id,created_at,starts_at,ends_at,event_id,influencer_id,status) values(100,now(),now(),now(),51,100,0);

INSERT INTO public.room_templates(id, event_id, scene_id, data) VALUES (501, 51, 'anything501', '{"property501": "value501"}');
INSERT INTO public.room_templates(id, event_id, scene_id, data) VALUES (502, 52, 'anything502', '{"property502": "value502"}');
INSERT INTO public.room_templates(id, event_id, scene_id, data) VALUES (504, 54, 'anything501', '{"property503": "value501"}');
INSERT INTO public.room_templates(id, event_id, scene_id, data) VALUES (505, 55, 'anything502', '{"property502": "value502"}');

INSERT INTO public.room_sessions(id, template_id, status, external_id, created_at, user_creator) VALUES (5001, 501, 'STARTED', 'external5001', now(), 81);
INSERT INTO public.room_sessions(id, template_id, status, external_id, created_at, user_creator) VALUES (5005, 505, 'SUSPENDED', 'external5005', now(), 81);
