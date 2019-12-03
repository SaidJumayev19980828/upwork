--inserting organizations(already inserted before the test class is loaded)
--INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
--INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());


-- integration Mapping types
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67001, 'PRODUCT');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67002, 'SHOP');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67003, 'ORDER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67004, 'CUSTOMER');
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67005, 'PAYMENT');


-- insert integration mapping
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id, created_at, updated_at) 
	VALUES(67001, 'LOCAL_VAL', 'OLD_REMOTE_VAL', 99001, now(), now());
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id, created_at, updated_at) 
	VALUES(67001, 'OLD_LOCAL_VAL', 'REMOTE_VAL', 99001, now(), now()) ;




