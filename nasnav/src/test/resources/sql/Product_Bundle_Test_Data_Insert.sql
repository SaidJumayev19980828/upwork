--dummy organization
INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');

insert into public.organizations(id , name )
values (99001, 'Bundle guys' );

-- dummy shop
INSERT INTO public.shops (id,"name",  organization_id) VALUES(100001 , 'Bundle Shop'  , 99001);


-- dummy products
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id) VALUES(200001, 'Bundle Product#1' , now() , now(), 0 , 99001);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id) VALUES(200002, 'Bundle Product#2' , now() , now(), 0 , 99001);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id) VALUES(200003, '#Bundle child' , now() , now() ,1 , 99001);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id) VALUES(200004, '#Bundle' , now() , now() ,1 , 99001);


--multiple dummy variants for product 1 only
insert into public.product_variants(id, "name" , product_id ) values(300011, 'Green product' , 200001);
insert into public.product_variants(id, "name" , product_id ) values(300012, 'blue product' , 200001);

--each product/bundle needs at least a single variant 
insert into public.product_variants(id, "name" , product_id ) values(300002, 'var' 	, 200002);
insert into public.product_variants(id, "name" , product_id ) values(300003, 'var' 	, 200003);
insert into public.product_variants(id, "name" , product_id ) values(300004, 'var' 	, 200004);


-- stocks for products, product variant and bundles
insert into public.stocks(id, shop_id , variant_id , quantity , price ,  organization_id)
values (400011, 100001 ,300011, 1 , 1000 , 99001);
insert into public.stocks(id, shop_id , variant_id , quantity , price,  organization_id)
values (400012, 100001, 300012, 20 , 122, 99001);
insert into public.stocks(id, shop_id , variant_id , quantity , price,  organization_id)
values (400002, 100001, 300002, 30 , 500, 99001);
insert into public.stocks(id, shop_id , variant_id , quantity , price,  organization_id)
values (400003, 100001, 300003, 2000 , 3000, 99001);
insert into public.stocks(id, shop_id , variant_id , quantity , price,  organization_id)
values (400004, 100001, 300004, 24 , 10000, 99001);



-- set child bundle items
insert into public.product_bundles(product_id, bundle_stock_id)
values(200003 , 400002);

-- set main bundle , which contains product#1 two variants and the child bundle
insert into public.product_bundles(product_id, bundle_stock_id)
values(200004 , 400011);
insert into public.product_bundles(product_id, bundle_stock_id)
values(200004 , 400012);
insert into public.product_bundles(product_id, bundle_stock_id)
values(200004 , 400003);
