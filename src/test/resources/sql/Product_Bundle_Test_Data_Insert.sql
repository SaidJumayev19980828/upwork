DELETE FROM public.product_bundles WHERE product_id IN (SELECT id FROM products WHERE organization_id BETWEEN 99000 AND 99999);
delete from public.role_employee_users where employee_user_id IN (SELECT id FROM public.employee_users where organization_id between 99000 and 99999);
delete from public.users where organization_id between 99000 and 99999;
delete from public.employee_users where organization_id between 99000 and 99999;
DELETE FROM public.roles WHERE organization_id BETWEEN 99000 AND 99999;
delete from public.stocks where organization_id between 99000 and 99999;
delete from public.product_variants where product_id IN (SELECT id from public.products where organization_id BETWEEN 99000 AND 99999);
delete from public.products where organization_id between 99000 and 99999;
delete from public.categories where id between 201 and 202;
delete from public.shops where organization_id between 99000 and 99999;
delete from public.brands where id between 101 and 102;
delete from public.brands where organization_id between 99000 and 99999;
DELETE FROM public.product_features where organization_id between 99000 and 99999;
delete from public.organizations where id between 99000 and 99999;
commit;

--dummy organization
insert into public.organizations(id , name , created_at, updated_at)
values (99001, 'Bundle guys' , now() , now());

-- dummy shop
INSERT INTO public.shops (id,"name", created_at , updated_at , organization_id) VALUES(100001 , 'Bundle Shop' , now() , now() , 99001);


-- dummy products
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id) VALUES(200001, 'Bundle Product#1' , now() , now(), 0 , 99001);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id) VALUES(200002, 'Bundle Product#2' , now() , now(), 0 , 99001);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id) VALUES(200003, '#Bundle child' , now() , now() ,1 , 99001);
insert into PUBLIC.products(ID,"name",created_at,updated_at,product_type, organization_id) VALUES(200004, '#Bundle' , now() , now() ,1 , 99001);


-- dummy variants for product 1 only
insert into public.product_variants(id, "name" , product_id ) values(300001, 'Green product' , 200001);
insert into public.product_variants(id, "name" , product_id ) values(300002, 'blue product' , 200001);


-- stocks for products, product variant and bundles
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price , created_at, updated_at, organization_id)
values (400001, 100001, 200001,300001, 1 , 1000 , now() , now(), 99001);
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400002, 100001, 200001,300002, 20 , 122, now() , now(), 99001);
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400003, 100001, 200002,null, 30 , 500, now() , now(), 99001);
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400004, 100001, 200003,null, 2000 , 3000, now() , now(), 99001);
insert into public.stocks(id, shop_id , product_id , variant_id , quantity , price, created_at, updated_at, organization_id)
values (400005, 100001, 200004,null, 2000 , 10000, now() , now(), 99001);



-- set child bundle items
insert into public.product_bundles(product_id, item_product_id, item_variant_id) 
values(200003 , 200002,null);

-- set main bundle , which contains product#1 two variants and the child bundle
insert into public.product_bundles(product_id, item_product_id, item_variant_id) 
values(200004 , 200001,300001);
insert into public.product_bundles(product_id, item_product_id, item_variant_id) 
values(200004 , 200001,300002);
insert into public.product_bundles(product_id, item_product_id, item_variant_id) 
values(200004 , 200003,null);


commit;