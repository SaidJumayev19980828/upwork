--inserting organizations(already inserted before the test class is loaded)
--INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
--INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());


-- integration parameters
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6600004, 'EXISTING_PARAM', FALSE);

-- inserting integration parameter value
insert into public.integration_param(id, param_type, organization_id, param_value)
values(55001, 6600004, 99001, 'old_val');


