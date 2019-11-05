--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());


INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(1, 'INTEGRATION_MODULE', TRUE);


INSERT INTO public.integration_param(id, param_type, organization_id, param_value, created_at, updated_at) 
VALUES(1, 1, 99001, 'com.nasnav.test.integration.modules.TestIntegrationModule', now(), now());
