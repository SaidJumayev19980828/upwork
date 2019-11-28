----------------------------inserting dummy data----------------------------
--inserting organizations
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99001, 'organization_1', now(), now());
INSERT INTO public.organizations(id, name, created_at, updated_at) VALUES (99002, 'organization_2', now(), now());

--inserting brands
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (101, 202, 'brand_1', now(), now(), 99002);
INSERT INTO public.brands(id, category_id, name,created_at, updated_at, organization_id) VALUES (102, 201, 'brand_2', now(), now(), 99001);

--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at, parent_id, logo,p_name) VALUES (201, 'category_1', now(), now(), null,'logo_1','name');
INSERT INTO public.categories(id, name, created_at, updated_at, parent_id, logo) VALUES (202, 'category_2', now(), now(), 201,'logo_2');
INSERT INTO public.categories(id, name, created_at, updated_at, parent_id, logo, p_name) VALUES (203, 'category_3', now(), now(), 201,'logo_3', 'category-3');
INSERT INTO public.categories(id, name, created_at, updated_at, parent_id, logo,p_name) VALUES (204, 'category_4', now(), now(), 202,'logo_4','category-4');

--inserting shops
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (501, 'shop_1', 102, now(), now(), 99002);
INSERT INTO public.shops(id, name, brand_id, created_at, updated_at, organization_id) VALUES (502, 'shop_2', 101, now(), now(), 99001);

--inserting Employee Users
INSERT INTO public.employee_users(id, name, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', now(), now(), 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id, created_at, updated_at, email, organization_id, authentication_token, shop_id)
	VALUES (69, now(), now(), 'testuser2@nasnav.com', 99001, 'hijkllm',  501);

--inserting Roles
insert into roles(id, name, created_at, updated_at, organization_id) values(1, 'NASNAV_ADMIN', now(), now(), 99001);
insert into roles(id, name, created_at, updated_at, organization_id) values(2, 'ORGANIZATION_ADMIN', now(), now(), 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (20, 68, 1, now(), now());
INSERT INTO public.role_employee_users(id, employee_user_id, role_id, created_at, updated_at) VALUES (21, 69, 2, now(), now());

--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1002, 'product_2',101, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',102, 203, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',102, 204, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6',102, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1008, 'product_8',102, 202, 99002, now(), now());


insert into Tags(id, name) values(5001, 'tag_1');
insert into Tags(id, name) values(5002, 'tag_2');
insert into Tags(id, name) values(5003, 'tag_3');
insert into Tags(id, name) values(5004, 'tag_4');
insert into Tags(id, name) values(5005, 'tag_5');
insert into Tags(id, name) values(5006, 'tag_6');
insert into Tags(id, name) values(5007, 'tag_7');

insert into organization_Tags(id, alias, tag_id, organization_id) values(5001,'tag_1', 5001, 99001);
insert into organization_Tags(id, alias, tag_id, organization_id) values(5002,'tag_2', 5002, 99001);
insert into organization_Tags(id, alias, tag_id, organization_id) values(5003,'tag_3', 5003, 99001);
insert into organization_Tags(id, alias, tag_id, organization_id) values(5004,'tag_4', 5004, 99001);
insert into organization_Tags(id, alias, tag_id, organization_id) values(5005,'tag_5', 5005, 99001);
insert into organization_Tags(id, alias, tag_id, organization_id) values(5006,'tag_6', 5006, 99001);

insert into tag_graph_edges(id, from_node, to_node, organization_id) values(5001, 5001, 5002, 99001);
insert into tag_graph_edges(id, from_node, to_node, organization_id) values(5002, 5001, 5003, 99001);
insert into tag_graph_edges(id, from_node, to_node, organization_id) values(5006, 5001, 5006, 99001);

insert into tag_graph_edges(id, from_node, to_node, organization_id) values(5003, 5002, 5004, 99001);
insert into tag_graph_edges(id, from_node, to_node, organization_id) values(5004, 5005, 5006, 99001);
insert into tag_graph_edges(id, from_node, to_node, organization_id) values(5007, 5005, 5003, 99001);
