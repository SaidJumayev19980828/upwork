----deleting extra_attributes data----
delete from public.extra_attributes where id between 701 and 702;

----inserting in extra_attributes table
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon, created_at, updated_at) VALUES (701, 'size', 'boolean', 402, '/uploads/category/fearutes/feature1.jpg', now(), now());
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon, created_at, updated_at) VALUES (702, 'size', 'boolean', 401, '/uploads/category/logo/logo1.jpg', now(), now());