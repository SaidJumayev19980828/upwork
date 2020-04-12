--inserting organizations
INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'org-number-one');
INSERT INTO public.organizations(id, name,  p_name) VALUES (99002, 'organization_2', 'org-number-two');

----inserting in extra_attributes table
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon)
    VALUES (701, 'size', 'boolean', 99002, '/uploads/category/fearutes/feature1.jpg');
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon)
    VALUES (702, 'filter', 'boolean', 99001, '/uploads/category/logo/logo1.jpg');