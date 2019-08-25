----------------------------deleting previous data----------------------------
delete from public.stocks where id between 601 and 604;
delete from public.products where id between 1001 and 1008;
delete from public.categories where id between 201 and 202;
delete from public.shops where id between 501 and 502;
delete from public.products where organization_id between 99000 and 99999;
delete from public.brands where id between 101 and 102;
delete from public.organizations where id between 99001 and 99002;

----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99002);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);

--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (201, 'category_1', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (202, 'category_2', now(), now());

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 99002);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 99001);

--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2',101, 201, 99002, now(), now(),'123456789');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',102, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',102, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6',102, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1008, 'product_8',102, 202, 99002, now(), now());

--inserting stocks
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, product_id) values(601, 502, 6, now(), now(), 99002, 600.0, 1001);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, product_id) values(602, 501, 8, now(), now(), 99001, 1200.0, 1002);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, product_id) values(603, 501, 4, now(), now(), 99002, 200.0, 1003);
insert into public.stocks(id, shop_id, quantity, created_at, updated_at, organization_id, price, product_id) values(604, 502, 6, now(), now(), 99001, 700.0, 1004);

