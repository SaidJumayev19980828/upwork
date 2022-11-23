--inserting organizations

INSERT INTO public.countries(id,name,iso_code,currency) values (100001, 'Egypt', 818, 'EGP');
INSERT INTO public.countries(id,name,iso_code,currency) values (100002, 'UK', 820, 'EGP');

INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'org-number-one');
INSERT INTO public.organizations(id, name,  p_name, extra_info, matomo) VALUES (99002, 'organization_2', 'org-number-two', '{"ALLOWED_COUNTRIES":[]}', 10);
INSERT INTO public.organizations(id, name,  p_name, extra_info) VALUES (99003, 'organization_3', 'org-number-three', '{"ALLOWED_COUNTRIES":[100002]}');


-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100001 , 'Bundle Shop'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100002 , 'another Shop'  , 99002);
INSERT INTO public.shops (id,"name",  organization_id, is_warehouse) values(100003 , 'warehouse', 99001, 1);

--inserting into notificationTopics
insert into public.notification_topics(id,topic) values(1,'topic1');
insert into public.notification_topics(id,topic) values(2,'topic2');

--insert employee
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  null);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser3@nasnav.com', 99001, 'abcdefg',  null);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id,notification_token)
VALUES (3, 'abcdefg', now(), 70, null,'cmplcFidnbv8xBJViEGqR3:APA91bFEHYDsEdoevXcRWy4OZ-dBfgGjf96MnA4RM-B6ILN_OWgL2mgq_vpKTWWtVQC6U04S9HUEIipI7Wvr2rz0u9Jr8WaTtRMeGbkv7bLj43XwWxzBHkkcU5V2CMD3uoYUkvshFgdx');
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id,notification_token)
VALUES (4, '123456', now(), 70, null,'cmplcFidnbv8xBJViEGqR3:APA91bFEHYDsEdoevXcRWy4OZ-dBfgGjf96MnA4RM-B6ILN_OWgL2mgq_vpKTWWtVQC6U04S9HUEIipI7Wvr2rz0u9Jr8WaTtRMeGbkv7bLj43XwWxzBHkkcU5V2CMD3uoYUkvshFgdx');


--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'STORE_MANAGER', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 2);

--inserting topics to user
INSERT INTO public.topic_employee_users(employee_user_id,topic_id) VALUES(70,1);
INSERT INTO public.topic_employee_users(employee_user_id,topic_id) VALUES(70,2);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
VALUES (89, 'user2@nasnav.com','user2','456', 99001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (5, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (6, '456', now(), null, 89);
