--inserting organizations(already inserted before the test class is loaded)
--INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
--INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);


-- integration parameters
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(6600004, 'EXISTING_PARAM', FALSE);

-- inserting integration parameter value
insert into public.integration_param(id, param_type, organization_id, param_value)
values(55001, 6600004, 99001, 'old_val');


