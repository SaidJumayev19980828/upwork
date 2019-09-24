--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at, p_name) VALUES (99001, 'organization_1', now(), now(), 'org-number-one');
INSERT INTO public.organizations(id, name, created_at, updated_at, p_name) VALUES (99002, 'organization_2', now(), now(), 'org-number-two');

----inserting in extra_attributes table
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon, created_at, updated_at)
    VALUES (701, 'size', 'boolean', 99002, '/uploads/category/fearutes/feature1.jpg', now(), now());
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon, created_at, updated_at)
    VALUES (702, 'filter', 'boolean', 99001, '/uploads/category/logo/logo1.jpg', now(), now());