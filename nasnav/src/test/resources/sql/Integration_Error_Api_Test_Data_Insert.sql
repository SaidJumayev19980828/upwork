INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(501 , 'Bundle Shop'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(502 , 'another Shop'  , 99001);


--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (70, 'testuser3@nasnav.com', 99001, 'sfeesdfsdf',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (3, 'sfeesdfsdf', now(), 70, null);


--inserting Roles
insert into public.roles(id, name,  organization_id) values(1, 'MEETUSVR_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 70, 4);


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



