
DELETE FROM public.integration_mapping where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.integration_param where  organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.integration_event_failure where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.integration_mapping_type;
DELETE FROM public.integration_param_type;
DELETE FROM public.products_extra_attributes WHERE extra_attribute_id IN (SELECT id FROM public.extra_attributes WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.extra_attributes WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.role_employee_users WHERE employee_user_id IN (SELECT id FROM public.employee_users WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.product_bundles WHERE product_id IN (SELECT id FROM public.products WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.baskets WHERE stock_id IN (SELECT Id from public.stocks where organization_id between 99000 and 99999);
DELETE FROM public.orders WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.payments WHERE user_id IN (SELECT Id from public.users where organization_id between 99000 and 99999);
DELETE FROM public.stocks WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.product_tags WHERE product_id IN (SELECT id from public.products where organization_id between 99000 and 99999);
DELETE FROM public.product_features where organization_id between 99000 and 99999;
DELETE FROM public.product_images WHERE
 product_id IN (SELECT id from public.products where organization_id between 99000 and 99999)
 or variant_id in (SELECT id FROM public.product_variants WHERE product_id IN (SELECT id FROM public.products WHERE organization_id BETWEEN 99000 AND 99999));
DELETE FROM public.product_variants WHERE product_id IN (SELECT id FROM public.products WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.products WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.roles WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.user_addresses WHERE user_id in (select id from public.users WHERE organization_id BETWEEN 99000 AND 99999)
  or emp_user_id in (select id from public.employee_users WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.addresses where id between 1001 and 1002;
DELETE FROM public.user_tokens WHERE user_id in (select id from users WHERE organization_id BETWEEN 99000 AND 99999)
  or employee_user_id in (select id from employee_users WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.users WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.employee_users WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.social_links WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.organization_images where organization_id between 99000 and 99999;
DELETE FROM public.files  where organization_id between 99000 and 99999;
DELETE FROM public.files  where orig_filename = 'nasnav--Test_Photo.png';  -- for test files with no organization
DELETE FROM public.scenes WHERE organization_id between 99000 and 99999;
DELETE FROM public.shop_sections WHERE organization_id between 99000 and 99999;
DELETE FROM public.shop_floors WHERE organization_id between 99000 and 99999;
DELETE FROM public.shop360s WHERE shop_id in (select id from public.shops WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.shops WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.brands WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.tag_graph_edges 
WHERE child_id IN (
	select node.id 
	from public.tag_graph_nodes node 
	left join tags tag on node.tag_id = tag.id 
	where tag.organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.tag_graph_nodes where tag_id  in (select id FROM public.tags WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.organization_themes WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.tags WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.organization_themes_settings where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.organization_theme_classes where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.organization_domains WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.organizations WHERE id BETWEEN 99000 AND 99999;
DELETE FROM public.organization_image_types;
DELETE FROM public.themes where id between 5001 and 5003;
DELETE FROM public.theme_classes where id between 990011 and 990012;
DELETE FROM public.categories WHERE id between 200 AND 240;
