delete from public.product_bundles where bundle_stock_id between 400001 and 400005;
delete from public.stocks where id between 400001 and 400005;
delete from public.product_variants where id between 300001 and 300002;
delete from public.products where id in (200001,200002,200003,200004);
delete from public.shops where id in (100001);
delete from public.organizations where id = 500001;