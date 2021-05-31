
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
INSERT INTO public.areas(id, "name", city_id)VALUES(2, 'Mokatem', 1);

INSERT INTO public.addresses(id, address_line_1, latitude, longitude, area_id, phone_number ,sub_area_id) values(12300001, 'supported address',30.057431, 31.481498, 1, '01111234567', null);
INSERT INTO public.addresses(id, address_line_1, latitude, longitude, area_id, phone_number ,sub_area_id) values(12300002, 'supported address',30.056400, 31.467494, 2, '01111234567', null);

--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

INSERT INTO public.categories(id, name) VALUES (201, 'category_1');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 201, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id, removed, priority) VALUES (501, 'shop_1', 102, 99002, 12300001, 0, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id, removed, priority) VALUES (502, 'shop_2', 101, 99001, 12300001, 0, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id, removed, priority) VALUES (503, 'shop_3', 102, 99001, 12300001, 0, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id, removed, priority) VALUES (504, 'shop_4', 102, 99001, 12300002, 0, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id, removed, priority) VALUES (505, 'shop_5', 102, 99001, 12300002, 0, 0);

INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser4@nasnav.com', 99001, '161718',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (71, 'testuser5@nasnav.com', 99001, '192021',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (158, 'testuser3@nasnav.com', 99002, '222324',  501);

INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at)
    VALUES (1001, 'product_1', 'product-one',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at)
    VALUES (1002, 'product_2', 'product-two',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, p_name, brand_id, category_id, organization_id, created_at, updated_at, product_type)
VALUES (1003, 'collection', 'collection',101, 201, 99001, now(), now(), 2);

insert into public.product_variants(id, "name" , product_id ) values(310001, 'var', 1001);
insert into public.product_variants(id, "name" , product_id ) values(310002, 'var', 1002);

insert into product_collections ("id", priority, product_id, variant_id)values (3600001,   2, 1003, 310002);

insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id)
values(601, 503, 3, 99001, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id)
values(602, 504, 3, 99001, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id)
values(603, 505, 3, 99001, 600.0, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id)
values(604, 502, 3, 99001, 600.0, 310002);

insert into Tags(id, name, alias, category_id, organization_id, metadata) values(5001, 'tag_1', 'tag_1', 201, 99001, '');

insert into product_tags(product_id, tag_id) values(1001, 5001);