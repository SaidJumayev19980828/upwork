----------------------------inserting dummy data----------------------------
--inserting organizations
INSERT INTO public.organizations(id, name) VALUES (99001, 'organization_1');
INSERT INTO public.organizations(id, name) VALUES (99002, 'organization_2');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting categories
INSERT INTO public.categories(id, name,  parent_id, logo,p_name) VALUES (201, 'category_1', null,'logo_1','name');
INSERT INTO public.categories(id, name,  parent_id, logo) VALUES (202, 'category_2', 201,'logo_2');
INSERT INTO public.categories(id, name,  parent_id, logo, p_name) VALUES (203, 'category_3', 201,'logo_3', 'category-3');
INSERT INTO public.categories(id, name,  parent_id, logo,p_name) VALUES (204, 'category_4', 202,'logo_4','category-4');
INSERT INTO public.categories(id, name,  logo,p_name) VALUES (205, 'category_5', 'logo_5','name');
INSERT INTO public.categories(id, name,  logo) VALUES (206, 'category_6', 'logo_6');
INSERT INTO public.categories(id, name,  logo, p_name) VALUES (207, 'category_7', 'logo_7', 'category-3');

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (501, 'shop_1', 102, 99002, 0);
INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) VALUES (502, 'shop_2', 101, 99001, 0);

--inserting Employee Users
INSERT INTO public.employee_users(id, name,  email, organization_id, authentication_token, shop_id)
	VALUES (68, 'Ahmad', 'testuser1@nasnav.com', 99001, 'abcdefg',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
	VALUES (69, 'testuser2@nasnav.com', 99001, 'hijkllm',  501);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (1, 'abcdefg', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (2, 'hijkllm', now(), 69, null);

--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);

--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 69, 2);

--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1002, 'product_2',101, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',102, 203, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',102, 204, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6',102, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1008, 'product_8',102, 202, 99002, now(), now());


insert into Tags(id, name, alias, category_id, organization_id, metadata, graph_id) values(5001,'tag_1', 'tag_1', 201, 99001, '', 99001);
insert into Tags(id, name, alias, category_id, organization_id, metadata, graph_id) values(5002,'tag_2', 'tag_2', 202, 99001, '', 99001);
insert into Tags(id, name, alias, category_id, organization_id, metadata, graph_id) values(5003,'tag_3', 'tag_3', null, 99001, '', 99001);
insert into Tags(id, name, alias, category_id, organization_id, metadata, graph_id) values(5004,'tag_4', 'tag_4', 204, 99001, '', 99001);
insert into Tags(id, name, alias, category_id, organization_id, metadata) values(5005,'tag_5', 'tag_5', 205, 99001, '');
insert into Tags(id, name, alias, category_id, organization_id, metadata, graph_id) values(5006,'tag_6', 'tag_6', 206, 99001, '', 99001);
insert into Tags(id, name, alias, category_id, organization_id, metadata, graph_id) values(5007,'tag_7', 'tag_7', 206, 99001, '', 99001);


INSERT INTO public.tag_graph_nodes (id, tag_id) VALUES(50011,5001);
INSERT INTO public.tag_graph_nodes (id, tag_id) VALUES(50012,5002);
INSERT INTO public.tag_graph_nodes (id, tag_id) VALUES(50013,5003);
INSERT INTO public.tag_graph_nodes (id, tag_id) VALUES(50014,5004);
INSERT INTO public.tag_graph_nodes (id, tag_id) VALUES(50015,5005);
INSERT INTO public.tag_graph_nodes (id, tag_id) VALUES(50016,5006);


insert into tag_graph_edges(id, parent_id, child_id) values(60012, 50011, 50012);
insert into tag_graph_edges(id, parent_id, child_id) values(60013, 50011, 50013);
insert into tag_graph_edges(id, parent_id, child_id) values(60014, 50011, 50016);

insert into product_tags(product_id, tag_id) values(1006, 5006);

