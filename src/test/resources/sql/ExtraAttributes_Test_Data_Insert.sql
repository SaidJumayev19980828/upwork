----------------------------deleting previous data----------------------------
delete from public.extra_attributes where id between 701 and 702;
delete from public.organizations where id between 99001 and 99002;

----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());

----inserting in extra_attributes table
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon, created_at, updated_at)
    VALUES (701, 'size', 'boolean', 99002, '/uploads/category/fearutes/feature1.jpg', now(), now());
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon, created_at, updated_at)
    VALUES (702, 'filter', 'boolean', 99001, '/uploads/category/logo/logo1.jpg', now(), now());