----------------------------inserting dummy data----------------------------

--inserting organizations
INSERT INTO public.organizations(id, name) VALUES (99001, 'organization_1');
INSERT INTO public.organizations(id, name) VALUES (99002, 'organization_2');

--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id) VALUES (501, 'shop_1', 102, 99002);
INSERT INTO public.shops(id, name, brand_id,  organization_id) VALUES (502, 'shop_2', 101, 99001);

--insering users
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'testuser3@nasnav.com', 99001, 'ssErf33',  502);



--inserting Roles
insert into roles(id, name,  organization_id) values(1, 'NASNAV_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(2, 'ORGANIZATION_ADMIN', 99001);
insert into roles(id, name,  organization_id) values(4, 'ORGANIZATION_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(5, 'STORE_EMPLOYEE', 99001);
insert into roles(id, name,  organization_id) values(3, 'CUSTOMER', 99001);


--inserting Roles EmployeeUsers relations
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (20, 68, 1);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (21, 68, 2);
INSERT INTO public.role_employee_users(id, employee_user_id, role_id) VALUES (22, 69, 2);


--inserting products
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1001, 'product_1',101, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at, barcode) VALUES (1002, 'product_2',101, 201, 99001, now(), now(),'ABCDEFG123');
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1003, 'product_3',101, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1004, 'product_4',102, 201, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1005, 'product_5',102, 202, 99001, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1006, 'product_6',102, 201, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1007, 'product_7',101, 202, 99002, now(), now());
INSERT INTO public.products(id, name, brand_id, category_id, organization_id, created_at, updated_at) VALUES (1008, 'product_8',102, 202, 99002, now(), now());



-- variants for each product
insert into public.product_variants(id, "name" , product_id, barcode ) values(310001, 'var' 	, 1001, 'ABCD123');
insert into public.product_variants(id, "name" , product_id, barcode  ) values(310002, 'var' 	, 1002, 'ABCDEFG123');
insert into public.product_variants(id, "name" , product_id, barcode  ) values(310003, 'var' 	, 1003, 'SFSDFE2232');
insert into public.product_variants(id, "name" , product_id, barcode  ) values(310004, 'var' 	, 1004, 'FDSFE2322');
insert into public.product_variants(id, "name" , product_id, barcode  ) values(310005, 'var' 	, 1005, 'VBVGFYDRF2');
insert into public.product_variants(id, "name" , product_id, barcode  ) values(310006, 'var' 	, 1006, 'FGZDFG234');
insert into public.product_variants(id, "name" , product_id, barcode  ) values(310007, 'var' 	, 1007, 'SDFGS34SFGS');
insert into public.product_variants(id, "name" , product_id, barcode  ) values(310008, 'var' 	, 1008, 'HKJLIKLJK45');



--inserting stocks
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(601, 502, 6, 99002, 600.00, 310001);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(602, 501, 8, 99001, 1200.0, 310002);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(603, 501, 4, 99002, 200.00, 310003);
insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id) values(604, 502, 6, 99001, 700.00, 310004);



INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(1, 'INTEGRATION_MODULE', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(2, 'MAX_REQUESTS_PER_SECOND', TRUE);
INSERT INTO public.integration_param_type(id, type_name, is_mandatory)VALUES(3, 'EXISTING_PARAM', FALSE);

INSERT INTO public.integration_param(id, param_type, organization_id, param_value)VALUES(1, 1, 99001, 'com.nasnav.test.integration.modules.TestIntegrationModule');
INSERT INTO public.integration_param(id, param_type, organization_id, param_value)VALUES(2, 2, 99001, '10');
insert into public.integration_param(id, param_type, organization_id, param_value)values(55001, 3, 99001, 'old_val');

-- integration Mapping types
INSERT INTO public.integration_mapping_type (id, type_name) VALUES(67001, 'PRODUCT_VARIANT');

INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id)VALUES(67001, '310001', '5', 99001) ;
INSERT INTO public.integration_mapping (mapping_type, local_value, remote_value, organization_id)VALUES(67001, '310002', '6', 99001) ;