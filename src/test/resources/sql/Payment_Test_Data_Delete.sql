----------------------------deleting previous data----------------------------
DELETE FROM public.baskets WHERE stock_id IN (SELECT Id from public.stocks where organization_id between 99000 and 99999);
delete from public.stocks where organization_id between 99000 and 99999;
delete from public.products where organization_id between 99000 and 99999;
delete from public.categories where id between 201 and 202;
delete from public.shops  where organization_id between 99000 and 99999;
delete from public.products where organization_id between 99000 and 99999;
delete from public.brands where id between 101 and 102;
delete from public.organizations where id between 99001 and 99002;
