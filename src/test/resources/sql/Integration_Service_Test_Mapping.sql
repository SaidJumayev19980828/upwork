INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);
INSERT INTO public.organizations(id, name) VALUES (99003, 'organization_3');

INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(1, 'INTEGRATION_MODULE', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(2, 'MAX_REQUESTS_PER_SECOND', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(3, 'EXISTING_PARAM', FALSE);

INSERT INTO public.integration_param(id, param_type, organization_id, param_value)
VALUES(1, 1, 99001, 'com.nasnav.test.integration.modules.TestIntegrationModule');
INSERT INTO public.integration_param(id, param_type, organization_id, param_value)
VALUES(2, 2, 99001, '10');
INSERT INTO public.integration_param(id, param_type, organization_id, param_value)
VALUES(3, 1, 99003, 'com.nasnav.test.integration.modules.TestIntegrationModule');
INSERT INTO public.integration_param(id, param_type, organization_id, param_value)
VALUES(4, 2, 99003, '5');
insert into public.integration_param(id, param_type, organization_id, param_value)
values(55001, 3, 99001, 'old_val');


-- integration Mapping types
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67001, 'PRODUCT_VARIANT');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67002, 'SHOP');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67003, 'ORDER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67004, 'CUSTOMER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67005, 'PAYMENT');


-- insert integration mapping
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) 
	VALUES(67001, 'LOCAL_VAL', 'OLD_REMOTE_VAL', 99001);
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id) 
	VALUES(67001, 'OLD_LOCAL_VAL', 'REMOTE_VAL', 99001) ;
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id)
	VALUES(67001, '310001', '5', 99001) ;




