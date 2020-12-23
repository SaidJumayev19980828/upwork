--inserting organizations

INSERT INTO public.countries(id,name,iso_code,currency) values (100001, 'Egypt', 819, 'EGP');
INSERT INTO public.countries(id,name,iso_code,currency) values (100002, 'UK', 820, 'EGP');

INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'org-number-one');
INSERT INTO public.organizations(id, name,  p_name, matomo) VALUES (99002, 'organization_2', 'org-number-two', 10);


-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100001 , 'Bundle Shop'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100002 , 'another Shop'  , 99002);
INSERT INTO public.shops (id,"name",  organization_id, is_warehouse) values(100003 , 'warehouse', 99001, 1);


----inserting in extra_attributes table
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon)
    VALUES (701, 'size', 'boolean', 99002, '/uploads/category/fearutes/feature1.jpg');
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon)
    VALUES (702, 'filter', 'boolean', 99001, '/uploads/category/logo/logo1.jpg');



INSERT INTO public.cities values(100001, 100001,'Cairo');
INSERT INTO public.cities values(100002, 100001,'Giza');
INSERT INTO public.cities values(100003, 100002,'London');

INSERT INTO public.areas values(100001, 'new cairo', 100001);
INSERT INTO public.areas values(100002, 'Nasr city', 100001);
INSERT INTO public.areas values(100003, 'Mohandiseen', 100002);
INSERT INTO public.areas values(100004, 'Dokki', 100002);
INSERT INTO public.areas values(100005, 'Agoza', 100002);


--insert employee
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  100001);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);


--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(3, 'STORE_MANAGER', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
