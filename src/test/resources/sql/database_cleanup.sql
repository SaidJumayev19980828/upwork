
DELETE FROM public.extra_attributes WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.role_employee_users WHERE employee_user_id IN (SELECT id FROM employee_users WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.employee_users WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.orders WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.stocks WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.product_bundles WHERE product_id IN (SELECT id FROM products WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.products WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.shops WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.brands WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.roles WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.users WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.social_links WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.Files WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.organizations WHERE id BETWEEN 99000 AND 99999;

