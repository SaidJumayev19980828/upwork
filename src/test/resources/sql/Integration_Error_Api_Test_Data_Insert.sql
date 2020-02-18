--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());



--inserting Employee Users
INSERT INTO public.employee_users(id, name, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', now(), now(), 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 99001, 'hijkllm',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (70, now(), now(), 'testuser3@nasnav.com', 99001, 'sfeesdfsdf',  501);

--inserting Roles
insert into public.roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);
insert into public.roles(id, name, created_at, updated_at, organization_id) values(4, 'ORGANIZATION_EMPLOYEE', now(), now(), 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (22, 70, 4, now(), now());


-- Mandatory integration parameters
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6601, 'INTEGRATION_MODULE', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6602, 'MAX_REQUESTS_PER_SECOND', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6603, 'DISABLED', FALSE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6604, 'EXISTING_PARAM', FALSE);


-- insert integration failures
INSERT INTO public.integration_event_failure (organization_id, event_type, event_data, created_at, handle_exception, fallback_exception)
VALUES(99001, 'com.event.ProductImportEvent', '{ data: "data..data..data"}', now(), 'caused by: things', 'caused by: other things');
INSERT INTO public.integration_event_failure (organization_id, event_type, event_data, created_at, handle_exception, fallback_exception)
VALUES(99001, 'com.event.ProductImportEvent', '{ data: "data..data..data"}', now() - interval '1 day', 'caused by: things', 'caused by: other things');
INSERT INTO public.integration_event_failure (organization_id, event_type, event_data, created_at, handle_exception, fallback_exception)
VALUES(99001, 'com.event.ProductImportEvent', '{ data: "data..data..data"}', now() - interval '2 day', 'caused by: things', 'caused by: other things');
INSERT INTO public.integration_event_failure (organization_id, event_type, event_data, created_at, handle_exception, fallback_exception)
VALUES(99001, 'com.event.ProductImportEvent', '{ data: "data..data..data"}', now() - interval '3 day', 'caused by: things', 'caused by: other things');
INSERT INTO public.integration_event_failure (organization_id, event_type, event_data, created_at, handle_exception, fallback_exception)
VALUES(99001, 'com.event.ProductImportEvent', '{ data: "data..data..data"}', now() - interval '4 day', 'caused by: things', 'caused by: other things');
INSERT INTO public.integration_event_failure (organization_id, event_type, event_data, created_at, handle_exception, fallback_exception)
VALUES(99001, 'com.event.ProductImportEvent', '{ data: "data..data..data"}', now() - interval '5 day', 'caused by: things', 'caused by: other things');
INSERT INTO public.integration_event_failure (organization_id, event_type, event_data, created_at, handle_exception, fallback_exception)
VALUES(99002, 'com.event.ProductImportEvent', '{ data: "data..data..data"}', now(), 'caused by: things', 'caused by: other things');



