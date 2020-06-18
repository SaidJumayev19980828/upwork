--inserting organizations
INSERT INTO public.organizations(id, name,  p_name) VALUES (99001, 'organization_1', 'org-number-one');
INSERT INTO public.organizations(id, name,  p_name) VALUES (99002, 'organization_2', 'org-number-two');


-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100001 , 'Bundle Shop'  , 99001);
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100002 , 'another Shop'  , 99002);



----inserting in extra_attributes table
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon)
    VALUES (701, 'size', 'boolean', 99002, '/uploads/category/fearutes/feature1.jpg');
INSERT INTO public.extra_attributes( id, key_name, attribute_type, organization_id, icon)
    VALUES (702, 'filter', 'boolean', 99001, '/uploads/category/logo/logo1.jpg');


INSERT INTO public.countries values (100001, 'Egypt');
INSERT INTO public.countries values (100002, 'UK');

INSERT INTO public.cities values(100001, 100001,'Cairo');
INSERT INTO public.cities values(100002, 100001,'Giza');
INSERT INTO public.cities values(100003, 100002,'London');

INSERT INTO public.areas values(100001, 'new cairo', 100001);
INSERT INTO public.areas values(100002, 'Nasr city', 100001);
INSERT INTO public.areas values(100003, 'Mohandiseen', 100002);
INSERT INTO public.areas values(100004, 'Dokki', 100002);
INSERT INTO public.areas values(100005, 'Agoza', 100002);
