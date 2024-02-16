DELETE FROM public.integration_mapping where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.integration_param where  organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.integration_event_failure where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.integration_mapping_type;
DELETE FROM public.integration_param_type;
DELETE FROM public.user_loyalty_transactions;
DELETE FROM public.user_loyalty_points;
DELETE FROM public.room_sessions;
DELETE FROM public.room_templates;
delete from public.event_requests;
delete from public.event_products;
delete from public.event_attachments;
delete from public.event_influencers;
delete from public.event_logs;
DELETE from public.contact_us;
DELETE FROM public.EVENTS;
delete from public.influencer_categories;
delete from public.influencers;
DELETE FROM public.cart_item_addon_details;
delete from addon_stocks;
delete from public.product_addons;
delete from addons;


DELETE FROM public.products_extra_attributes WHERE extra_attribute_id IN (SELECT id FROM public.extra_attributes WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.extra_attributes WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.role_employee_users WHERE employee_user_id IN (295);
DELETE FROM public.role_employee_users WHERE employee_user_id IN (SELECT id FROM public.employee_users WHERE organization_id > 99000);
DELETE FROM public.product_bundles WHERE product_id IN (SELECT id FROM public.products WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.shipment where id IN (
	SELECT shp.id FROM public.shipment shp
	LEFT JOIN public.orders ord on shp.sub_order_id = ord.id
	WHERE ord.organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.organization_shipping_service WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.shipping_areas;
DELETE FROM public.shipping_service;

DELETE FROM meta_orders_promotions;
DELETE FROM public.PROMOTIONS_CART_CODES;
DELETE FROM public.PROMOTIONS_CODES_USED;
DELETE FROM public.promotions;

DELETE FROM public.rocket_chat_organization_departments;
DELETE FROM public.rocket_chat_employee_agents;

DELETE FROM public.cart_item_addon_details;
DELETE FROM public.cart_items where id in (
	select crt.id
	from public.cart_items crt
	left join public.users usr on crt.user_id  = usr.id
	where usr.organization_id between 99000 and 99999
);
DELETE FROM public.return_request_item WHERE return_request_id IN
     (SELECT Id from public.return_request where meta_order_id in
         (select id from public.meta_orders where organization_id between 99000 and 99999));
DELETE FROM public.return_request where meta_order_id in
     (select id from public.meta_orders where organization_id between 99000 and 99999);
DELETE FROM public.return_shipment where id in (
    select return_shipment_id
    from public.return_request_item it
    left join public.return_request req
    on it.return_request_id = req.id
    inner join public.meta_orders meta
    on req.meta_order_id = meta.id
    and meta.organization_id between 99000 and 99999
);
DELETE FROM saved_posts;
DELETE FROM public.post_attachments;
DELETE FROM public.post_likes;
DELETE FROM public.post_clicks;
DELETE FROM public.post_products;
DELETE FROM public.post_transactions;
DELETE FROM public.posts;
delete from public.bank_account_activities;
delete from public.bank_inside_transactions;
delete from public.bank_outside_transactions;
delete from public.bank_reservations;
delete from public.bank_accounts;
DELETE FROM public.advertisement_product;
DELETE FROM public.advertisements;
DELETE FROM public.user_followers;
DELETE FROM public.return_shipment where shipping_service_id = 'TEST';
DELETE FROM public.loyalty_spent_transactions WHERE transaction_id in (select id from loyalty_point_transactions WHERE org_id between 99000 and 99999);
DELETE FROM public.loyalty_point_transactions WHERE org_id between 99000 and 99999;
DELETE FROM public.baskets WHERE stock_id IN (SELECT Id from public.stocks where organization_id between 99000 and 99999);
DELETE FROM public.orders WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.payments WHERE user_id IN (SELECT Id from public.users where organization_id between 99000 and 99999);
DELETE FROM public.meta_orders where organization_id between 99000 and 99999;
DELETE FROM public.stocks WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.units;
DELETE FROM public.product_tags WHERE product_id IN (SELECT id from public.products where organization_id between 99000 and 99999);
DELETE FROM public.variant_feature_values where feature_id in (select id from public.product_features where organization_id between 99000 and 99999);
DELETE FROM public.product_features where organization_id between 99000 and 99999;
DELETE FROM public.scheduler_tasks;
DELETE FROM public.availabilities;
DELETE FROM public.product_images WHERE
 product_id IN (SELECT id from public.products where organization_id between 99000 and 99999)
 or variant_id in (SELECT id FROM public.product_variants WHERE product_id IN (SELECT id FROM public.products WHERE organization_id BETWEEN 99000 AND 99999));
DELETE FROM public.product_collections WHERE product_id IN (SELECT id FROM public.products WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.product_ratings where variant_id in
    (SELECT id FROM public.product_variants WHERE product_id IN (SELECT id FROM public.products WHERE organization_id BETWEEN 99000 AND 99999));
DELETE FROM public.product_variants WHERE product_id IN (SELECT id FROM public.products WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.shop360_products where shop_id in (select id from public.shops WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.products WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.roles;
DELETE FROM public.user_addresses WHERE user_id in (select id from public.users WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.user_tokens WHERE user_id in (select id from users WHERE organization_id BETWEEN 99000 AND 99999)
  or employee_user_id in (select id from employee_users WHERE organization_id > 99000);
DELETE FROM public.yeshtery_user_otp WHERE user_id in (SELECT id FROM public.yeshtery_users WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.user_otp WHERE user_id in (SELECT id FROM public.users WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.employee_user_otp WHERE user_id in
            (SELECT id FROM public.employee_users WHERE organization_id > 99000);
DELETE FROM public.yeshtery_user_tokens WHERE yeshtery_user_id in (SELECT id FROM public.yeshtery_users WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.yeshtery_users WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.api_logs WHERE organization_id between 99000 AND 99999;
DELETE FROM public.rocket_chat_customer_tokens WHERE user_id in (select id from public.users WHERE organization_id BETWEEN 99000 AND 99999);
delete from public.files_resized;
DELETE FROM public.files  where user_id in (SELECT id from public.users where organization_id between 99000 and 99999);
DELETE FROM public.video_chat_logs WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.users WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.package_registered WHERE org_id > 99000;
DELETE FROM public.employee_user_heart_beats_logs;
DELETE FROM public.employee_users WHERE organization_id > 99000;
DELETE FROM public.social_links WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.organization_images where organization_id between 99000 and 99999;
DELETE FROM public.files  where organization_id between 99000 and 99999;
DELETE FROM public.files  where orig_filename = 'nasnav--Test_Photo.png';  -- for test files with no organization
DELETE FROM public.scenes WHERE organization_id between 99000 and 99999;
DELETE FROM public.shop_sections WHERE organization_id between 99000 and 99999;
DELETE FROM public.shop_floors WHERE organization_id between 99000 and 99999;
DELETE FROM public.shop360s WHERE shop_id in (select id from public.shops WHERE organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.loyalty_point_config WHERE  organization_id between 99000 and 99999;
DELETE FROM public.loyalty_pins WHERE shop_id in (select id from shops where organization_id BETWEEN 99000 AND 99999);
DELETE FROM public.addon_stocks;
DELETE FROM public.shops WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.addresses;
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
DELETE FROM public.settings  WHERE organization_id BETWEEN 99000 AND 99999;

DELETE FROM public.organiztion_cart_optimization  WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.user_subscriptions where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.organization_payments where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.seo_keywords where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.sub_areas where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.loyalty_family;
DELETE FROM public.loyalty_booster;
DELETE FROM public.loyalty_points  where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.loyalty_tier  where organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.addons ;

DELETE FROM public.chat_widget_setting;
DELETE FROM public.organizations ;
DELETE FROM public.shipping_areas;
DELETE FROM public.areas;
DELETE FROM public.cities;
DELETE FROM public.subscription;
DELETE FROM public.package_service;
DELETE FROM public.service_instance;
DELETE FROM public.package;
DELETE FROM public.service;
DELETE FROM public.countries;
DELETE FROM public.group_video_chat_log_employee_user;
DELETE FROM public.group_video_chat_log_user;
DELETE FROM public.group_video_chat_logs;
DELETE FROM public.organization_image_types;
DELETE FROM public.themes where id between 5001 and 5003;
DELETE FROM public.theme_classes where id between 990011 and 990012;
DELETE FROM public.categories WHERE id between 200 AND 240;

DELETE FROM public.services_registered_in_package;
DELETE FROM public.package WHERE id > 99000;

delete from public.loyalty_point_types where id between  31001 and 31999;
DELETE FROM public.stripe_customer;





