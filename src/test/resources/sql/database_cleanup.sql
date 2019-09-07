
DELETE FROM public.extra_attributes WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.role_employee_users WHERE employee_user_id IN (SELECT id FROM employee_users WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.product_bundles WHERE product_id IN (SELECT id FROM products WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.baskets WHERE stock_id IN (SELECT Id from public.stocks where organization_id between 99000 and 99999);
DELETE FROM public.orders WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.stocks WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.product_features where organization_id between 99000 and 99999;
DELETE FROM public.product_images WHERE product_id IN (SELECT id from public.products where organization_id between 99000 and 99999);
DELETE FROM public.product_variants WHERE product_id IN (SELECT id FROM products WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.products WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.roles WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.users WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.employee_users WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.social_links WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.files  where organization_id between 99000 and 99999;
DELETE FROM public.files  where orig_filename = 'nasnav--Test_Photo.png';  -- for test files with no organization
DELETE FROM public.shops WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.brands WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.malls where id = 901;
DELETE FROM public.organization_themes WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.organizations WHERE id BETWEEN 99000 AND 99999;
DELETE FROM public.categories WHERE id between 200 AND 205;

