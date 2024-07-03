-- ----------------------------inserting dummy data----------------------------

--inserting organizations

INSERT INTO public.countries(id,name,iso_code,currency) values (100001, 'Egypt', 818, 'EGP');
INSERT INTO public.countries(id,name,iso_code,currency) values (100002, 'UK', 820, 'EGP');

INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'org-number-one');
INSERT INTO public.organizations(id, name,  p_name, extra_info, matomo) VALUES (99002, 'organization_2', 'org-number-two', '{"ALLOWED_COUNTRIES":[]}', 10);
INSERT INTO public.organizations(id, name,  p_name, extra_info) VALUES (99003, 'organization_3', 'org-number-three', '{"ALLOWED_COUNTRIES":[100002]}');
INSERT INTO public.organizations(id, name, currency_iso) VALUES (63, 'MeetusAR', 820);


--insert employee
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser1@nasnav.com', 63, 'nopqrst',  null);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser2@nasnav.com', 99003, 'hijkllm',  null);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (71, 'admin@nasnav.com', 63, 'abcdefg',  null);


--inserting Roles
insert into public.roles(id, name,  organization_id) values(30, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(40, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(41, 'ORGANIZATION_ADMIN', 99003);
insert into public.roles(id, name,  organization_id) values(50, 'STORE_MANAGER', 99001);
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',null, null, 99001, now(), now());

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (40, 69, 50);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (19, 'nopqrst', now(), 69, null);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (41, 70, 41);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (20, 'hijkllm', now(), 70, null);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (42, 71, 30);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (21, 'abcdefg', now(), 71, null);

truncate TABLE public.product_3d_model;
insert into product_3d_model (id, name, description, model, size, barcode, sku, color)
values (22,'3dm-model','description-test','model1',6,'test-barcode2','sku-test2','blue');
insert into product_3d_model (id, name, description, model, size, barcode, sku, color)
values (23,'3dm-model','description-test','model2',6,'test-barcode3','sku-test3','red');
insert into product_3d_model (id, name, description, model, size, barcode, sku, color)
values (24,'3dm-model','description-test','model3',6,'test-barcode4','sku-test4','blue');

INSERT INTO public.products(id, name, brand_id, category_id, organization_id, model_id, created_at, updated_at) VALUES (1000, 'product_2',null, null, 99001, 24, now(), now());
