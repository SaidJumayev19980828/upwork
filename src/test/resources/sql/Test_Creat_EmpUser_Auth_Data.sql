delete from public.organizations where id between 801 and 802;
delete from public.employee_users where id in(68, 69);
delete from public.roles where id between 1 and 3;
delete from public.role_employee_users where id between 20 and 21;
delete from public.shops where id between 10 and 11;

--///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (801, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (802, 'organization_2', now(), now());

INSERT INTO public.shops(id, name, created_at, updated_at) VALUES (10, 'shop_1', now(), now());

INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (68, now(), now(), 'testuser1@nasnav.com', 801, 'abcdefg',  10);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 801, 'hijkllm',  10);



insert into roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 801);
insert into roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 801);
insert into roles(id, name, created_at, updated_at, organization_id) values(3, 'CUSTOMER', now(), now(), 801);

INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at)
        VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at)
        VALUES (21, 69, 2, now(), now());