ALTER TABLE public.addons
ADD CONSTRAINT fk_addons_organization
FOREIGN KEY (organization_id)
REFERENCES public.organizations (id);


ALTER TABLE public.product_addons
ADD CONSTRAINT fk_product_addons_product
FOREIGN KEY (product_id)
REFERENCES public.products (id);


ALTER TABLE public.product_addons
ADD CONSTRAINT fk_product_addons_addon
FOREIGN KEY (addon_id)
REFERENCES public.addons (id);


ALTER TABLE public.addon_stocks
ADD CONSTRAINT fk_addon_stocks_shop
FOREIGN KEY (shop_id)
REFERENCES public.shops (id);


ALTER TABLE public.addon_stocks
ADD CONSTRAINT fk_addon_stocks_addon
FOREIGN KEY (addon_id)
REFERENCES public.addons (id);


ALTER TABLE public.cart_item_addon_details
ADD CONSTRAINT fk_cart_item_addon_details_addon_stock
FOREIGN KEY (addon_stock_id)
REFERENCES public.addon_stocks (id);


ALTER TABLE public.cart_item_addon_details
ADD CONSTRAINT fk_cart_item_addon_details_cart_item
FOREIGN KEY (cart_item_id)
REFERENCES public.cart_items (id);


ALTER TABLE public.cart_item_addon_details
ADD CONSTRAINT fk_cart_item_addon_details_user
FOREIGN KEY (user_id)
REFERENCES public.users (id);
