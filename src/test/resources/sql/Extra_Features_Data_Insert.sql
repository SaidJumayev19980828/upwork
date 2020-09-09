
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

insert into public.roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);

INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2',101, 201, 99002, now(), now(),'123456789');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode, removed) VALUES (1003, 'product_3',101, 201, 99002, now(), now(),'123456789', 1);

insert into public.product_variants(id, "name" , product_id ) values(310002, 'var' 	, 1002);
insert into public.product_variants(id, "name" , product_id ,removed) values(310003, 'var' 	, 1003, 1);

INSERT INTO public.extra_attributes(id, key_name, attribute_type, organization_id, icon) VALUES(11002, 'name_1', 'type', 99002, 'icon');
INSERT INTO public.extra_attributes(id, key_name, attribute_type, organization_id, icon) VALUES(11003, 'name_2', 'type', 99002, 'icon');
INSERT INTO public.extra_attributes(id, key_name, attribute_type, organization_id, icon) VALUES(11004, 'name_3', 'type', 99002, 'icon');

INSERT INTO public.products_extra_attributes(id,extra_attribute_id, value, variant_id) VALUES(11003, 11003, 'value', 310002);
INSERT INTO public.products_extra_attributes(id,extra_attribute_id, value, variant_id) VALUES(11004, 11004, 'value', 310003);
