----------------------------deleting previous data----------------------------
delete from public.extra_attributes where id between 701 and 702;
delete from public.organizations where id between 801 and 802;

----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (801, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (802, 'organization_2', now(), now());

----inserting in extra_attributes table
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon, created_at, updated_at)
    VALUES (701, 'size', 'boolean', 802, '/uploads/category/fearutes/feature1.jpg', now(), now());
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon, created_at, updated_at)
    VALUES (702, 'filter', 'boolean', 801, '/uploads/category/logo/logo1.jpg', now(), now());