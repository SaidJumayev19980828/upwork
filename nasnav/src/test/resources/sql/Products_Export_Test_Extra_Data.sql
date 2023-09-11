INSERT INTO public.units SELECT r, CONCAT('unit_', r) FROM generate_series(500000, 501001) r;

INSERT INTO public.shops(id, name, brand_id,  organization_id, removed) SELECT r, CONCAT('shop_', r), 101, 99001, 0 FROM generate_series(500000, 501001) r;

insert into public.stocks(id, shop_id, quantity,  organization_id, price, variant_id, unit_id) SELECT r, r, 4, 99001, 400.0, 310001, r FROM generate_series(500000, 501001) r;
