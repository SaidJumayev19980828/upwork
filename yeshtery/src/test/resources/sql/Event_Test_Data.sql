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
VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (89, 'user2@nasnav.com','user2','456', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, '456', now(), null, 89);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  503);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (71, 'testuser5@nasnav.com', 99001, '192021',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (6, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (7, '161718', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (8, 'abcdefg', now(), 70, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (9, '192021', now(), 71, null);

insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(6, 'STORE_MANAGER', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);

INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (19, 68,2);

INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (23, 71, 6);

INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());

INSERT INTO public.influencers(id,created_at,employee_user_id,approved) values (100,now(),71,false);
INSERT INTO public.influencers(id,created_at,employee_user_id,approved) values (101,now(),70,true);

INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(100,now(),CURRENT_DATE + INTERVAL '1 day',CURRENT_DATE + INTERVAL '2 day',99001,false,'name','desc',0);
INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(101,now(),CURRENT_DATE - INTERVAL '30 day',CURRENT_DATE + INTERVAL '30 day',99001,false,'name','desc',0);
INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(102,now(),CURRENT_DATE - INTERVAL '20 day',CURRENT_DATE + INTERVAL '20 day',99001,false,'name','desc',0);
INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(103,now(),CURRENT_DATE - INTERVAL '17 day',CURRENT_DATE + INTERVAL '17 day',99001,false,'name','desc',0);
INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(104,now(),CURRENT_DATE - INTERVAL '12 day',CURRENT_DATE + INTERVAL '12 day',99001,false,'name','desc',0);

INSERT INTO public.event_requests(id,created_at,starts_at,ends_at,event_id,influencer_id,status) values(100,now(),now(),now(),100,100,0);
