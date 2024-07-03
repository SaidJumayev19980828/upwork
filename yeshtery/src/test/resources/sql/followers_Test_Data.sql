
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'fortune');
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting organization domain
INSERT INTO public.organization_domains (id, "domain", organization_id, canonical) VALUES(150001, 'fortune.nasnav.com', 99001, 0);
INSERT INTO public.organization_domains (id, "domain", organization_id, canonical) VALUES(150002, 'www.fortune.com', 99001, 1);

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (103, 201, 'brand_3', 99001);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (104, 201, 'brand_4', 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 102, 99001, 0);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (89, 'user2@nasnav.com','user2','456', 99002);
INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (90, 'user3@nasnav.com', 'user3', '554', 99001);
INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (91, 'user4@nasnav.com', 'user4', '778', 99002);
INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (92, 'user5@nasnav.com', 'user5', '741', 99001);
INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (93, 'user6@nasnav.com', 'user6', '268', 99002);
INSERT INTO public.users(id, email, user_name, authentication_token, organization_id)
VALUES (94, 'user7@nasnav.com', 'user7', '744', 99002);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (101, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (102, '456', now(), null,89);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (103, '554', now(), null, 90);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (104, '778', now(), null, 91);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (105, '741', now(), null, 92);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (106, '268', now(), null, 93);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (107, '744', now(), null, 94);

--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser3@nasnav.com', 99002, '123456',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, '123456', now(), 70, null);
--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 2);

--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Shoe size', 's-size', 'Size of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Shoe color', 's-color', 'Color of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Shoe size', 's-size', 'Size of the shoes', 99002);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(238,'size', 'size', 'Size', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

-- insert products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at)
VALUES (1001, 'product_1',101, 201, 99001, now(), now()),
       (1002, 'product_2',101, 201, 99001, now(), now());


INSERT INTO public.user_followers(id, user_id, follower_id) VALUES (1, 89, 88);
INSERT INTO public.user_followers(id, user_id, follower_id) VALUES (2, 90, 88);
INSERT INTO public.user_followers(id, user_id, follower_id) VALUES (3, 91, 88);
INSERT INTO public.user_followers(id, user_id, follower_id) VALUES (4, 88, 91);
INSERT INTO public.user_followers(id, user_id, follower_id) VALUES (5, 89, 91);

INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(103,now(),CURRENT_DATE - INTERVAL '14 day',CURRENT_DATE + INTERVAL '14 day',99001,false,'event1','desc',0);
INSERT INTO public.events(id,created_at,starts_at,ends_at,organization_id,visible,name,description,status) values(104,now(),CURRENT_DATE - INTERVAL '12 day',CURRENT_DATE + INTERVAL '12 day',99001,false,'name','desc',1);

INSERT INTO public.event_logs(id, created_at, interested_at, attend_at, event_id, user_id)
VALUES (101, CURRENT_DATE, CURRENT_DATE + INTERVAL '2 day', CURRENT_DATE + INTERVAL '7 day', 103, 88);
INSERT INTO public.event_logs(id, created_at, interested_at, attend_at, event_id, user_id)
VALUES (103, CURRENT_DATE, CURRENT_DATE + INTERVAL '2 day', null, 104, 88);
