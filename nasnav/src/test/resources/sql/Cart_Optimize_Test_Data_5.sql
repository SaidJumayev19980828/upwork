----------------------------inserting dummy data----------------------------
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');
INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.cities(id,country_id, "name") VALUES(3,1, 'Alexandria');
INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
INSERT INTO public.areas(id, "name", city_id)VALUES(144, 'Abu Kir', 3);

--inserting organizations
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99001, 'organization_1', 818);
INSERT INTO public.organizations(id, name, currency_iso) VALUES (99002, 'organization_2', 818);

--inserting sub-areas
INSERT INTO public.sub_areas(id, area_id, name, organization_id)values(77001, 1, 'Badr City', 99001);
INSERT INTO public.sub_areas(id, area_id, name, organization_id)values(77002, 1, 'Shorouk City', 99001);
INSERT INTO public.sub_areas(id, area_id, name, organization_id)values(77003, 1, 'Too far City', 99001);


--inserting brands
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (101, 202, 'brand_1', 99002);
INSERT INTO public.brands(id, category_id, name, organization_id) VALUES (102, 201, 'brand_2', 99001);

--inserting categories
INSERT INTO public.categories(id, name) VALUES (201, 'category_1');
INSERT INTO public.categories(id, name) VALUES (202, 'category_2');

--inserting product features
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(234,'Shoe size', 's-size', 'Size of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(235,'Shoe color', 's-color', 'Color of the shoes', 99001);
INSERT INTO public.product_features(id, name, p_name, description, organization_id)VALUES(236,'Shoe size', 's-size', 'Size of the shoes', 99002);

INSERT INTO public.addresses(id, address_line_1, area_id, phone_number, sub_area_id) values(12300001, 'address line', 1, '01111234567', 77001);
INSERT INTO public.addresses(id, address_line_1, area_id, phone_number, sub_area_id) values(12300002, 'address line', 144, '01111234567', 77002);
INSERT INTO public.addresses(id, address_line_1, area_id, phone_number, sub_area_id) values(12300003, 'with no supported sub-area', 144, '01111234567', 77003);


--inserting shops
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (501, 'shop_1', 102, 99001, 12300001);
INSERT INTO public.shops(id, name, brand_id,  organization_id, address_id) VALUES (502, 'shop_2', 101, 99001, 12300002);

--insering employees
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (68, 'testuser1@nasnav.com', 99001, '101112',  502);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (69, 'testuser2@nasnav.com', 99002, '131415',  501);
INSERT INTO public.employee_users(id,  email, organization_id, authentication_token, shop_id)
VALUES (70, 'ahmed.galal@nasnav.com', 99001, '161718',  502);

INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700001, '101112', now(), 68, null);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700002, '131415', now(), 69, null);

--inserting users
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (88, 'user1@nasnav.com','user1','123', 99001);
INSERT INTO public.users(id, email,  user_name, authentication_token, organization_id)
    VALUES (89, 'test2@nasnav.com','user2','456', 99001);


INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700003, '123', now(), null, 88);
INSERT INTO public.user_tokens(id, token, update_time, employee_user_id, user_id) VALUES (700004, '456', now(), null, 89);


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

-- variants for each product
insert into public.product_variants(id, "name" , product_id, feature_spec ) values(310001, 'var', 1001, '{"234":"66"}');

insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, currency) values(601, 501, 0, 99001, 1200.0, 310001, 1);

-- insert cart
INSERT INTO public.cart_items (id, stock_id, quantity, user_id, is_wishlist, created_at) VALUES(2000, 601, null, 88, 1, now());
INSERT INTO public.cart_items (id, stock_id, quantity, user_id, is_wishlist, created_at) VALUES(2001, 601, 1, 88, 0, now());


INSERT INTO public.User_addresses(id, user_id, address_id, principal) values(12300001, 88, 12300001, false);
INSERT INTO public.User_addresses(id, user_id, address_id, principal) values(12300003, 88, 12300001, false);

insert into organiztion_cart_optimization (optimization_parameters, optimization_strategy, organization_id, shipping_service_id)
values (
'{
  "sub_area_shop_mapping":{
    "77001": 501,
    "77002": 502
  }
}','SHOP_PER_SUBAREA', 99001, null);
