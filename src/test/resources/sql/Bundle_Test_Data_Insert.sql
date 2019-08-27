
--dummy organization
insert into public.organizations(id , name , created_at, updated_at)
values (99001, 'Bundle guys' , now() , now());
insert into public.organizations(id , name , created_at, updated_at)
values (99002, 'Other Bundle guys' , now() , now());

--inserting categories
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (201, 'category_1', now(), now());
INSERT INTO public.categories(id, name, created_at, updated_at) VALUES (202, 'category_2', now(), now());

-- dummy shop
INSERT INTO public.shops (id,"name", created_at , updated_at , organization_id) VALUES(100001 , 'Bundle Shop' , now() , now() , 99001);


-- dummy products
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200001, 'Bundle Product#1' , now() , now(), 0 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200002, 'Bundle Product#2' , now() , now(), 0 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200003, '#Bundle child' , now() , now() ,1 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200004, '#Bundle 4' , now() , now() ,1 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200005, '#Bundle 3' , now() , now() ,1 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200006, '#Bundle 2' , now() , now() ,1 , 99001, 201);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id,category_id) VALUES(200007, '#Bundle 1' , now() , now() ,1 , 99002, 202);


-- dummy variants for product 1 only
insert into public.product_variants(id, "name" , product_id ) values(300001, 'Green product' 	, 200001);
insert into public.product_variants(id, "name" , product_id ) values(300002, 'blue product' 	, 200001);
insert into public.product_variants(id, "name" , product_id ) values(300003, 'big product' 		, 200001);
insert into public.product_variants(id, "name" , product_id ) values(300004, 'small product' 	, 200001);


-- stocks for variants
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400001, 100001, 200001,300001, 1 , 1000 , now() , now(), 99001);
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400002, 100001, 200001,300002, 20 , 122, now() , now(), 99001);
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400003, 100001, 200001,300003, 20 , 3.5, now() , now(), 99001);
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400004, 100001, 200001,300004, 20 , 88, now() , now(), 99001);

-- stocks for products and bundles(which are also products)
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400005, 100001, 200002,null, 30 , 500, now() , now(), 99001);
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400006, 100001, 200003,null, 2000 , 3000, now() , now(), 99001);
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400007, 100001, 200004,null, 2000 , 10000, now() , now(), 99001);



-- set child bundle items
insert into public.product_bundles(product_id, bundle_stock_id) values(200003 , 400005);

-- set main bundle , which contains product#1 2 variants and the child bundle
insert into public.product_bundles(product_id, bundle_stock_id) values(200004 , 400001);
insert into public.product_bundles(product_id, bundle_stock_id) values(200004 , 400002);
insert into public.product_bundles(product_id, bundle_stock_id) values(200004 , 400003);

-- other bundles
insert into public.product_bundles(product_id, bundle_stock_id) values(200005 , 400004);
insert into public.product_bundles(product_id, bundle_stock_id) values(200006 , 400006);
insert into public.product_bundles(product_id, bundle_stock_id) values(200007 , 400006);


commit;
