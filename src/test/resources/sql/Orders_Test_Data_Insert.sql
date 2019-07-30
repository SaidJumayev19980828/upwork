----------------------------deleting previous data----------------------------
delete from public.orders where id between 32 and 48;
delete from public.users where id in(88,89);
delete from public.shops where id between 501 and 502;
delete from public.organizations where id between 99001 and 99002;

----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());


--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99002);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 99002);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 99001);

--inserting users
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com',now(), now(), 'user1','abdcefg', 99001);
INSERT INTO public.users(id, email, created_at, updated_at, user_name, authentication_token, organization_id)
    VALUES (89, 'user2@nasnav.com',now(), now(), 'user2','hijklm', 99002);

--inserting orders
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(33, 88, now(), now(), 99001, 0, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(37, 88, now(), now(), 99002, 0, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(38, 88, now(), now(), 99001, 1, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(39, 88, now(), now(), 99002, 1, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(40, 88, now(), now(), 99001, 0, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(41, 88, now(), now(), 99002, 1, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(42, 88, now(), now(), 99001, 1, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(43, 88, now(), now(), 99002, 0, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(34, 89, now(), now(), 99001, 0, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(35, 89, now(), now(), 99002, 0, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(36, 89, now(), now(), 99001, 1, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(44, 89, now(), now(), 99002, 1, 501);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(45, 89, now(), now(), 99001, 0, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(46, 89, now(), now(), 99002, 1, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(47, 89, now(), now(), 99001, 1, 502);
insert into orders(id,user_id,created_at, updated_at, organization_id,status,shop_id) values(48, 89, now(), now(), 99002, 0, 502);