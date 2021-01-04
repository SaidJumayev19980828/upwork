INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(3,'Nigeria', 556, 'NGN');

INSERT INTO public.cities(id,country_id, "name") VALUES(1001,3, 'Cairo');
INSERT INTO public.cities(id,country_id, "name") VALUES(1002,3, 'Alexandria');

INSERT INTO public.areas(id, "name", city_id)VALUES(5555, 'New Cairo', 1001);
INSERT INTO public.areas(id, "name", city_id)VALUES(4444, 'Mokatem', 1002);

INSERT INTO public.shipping_service(id)VALUES('CLICKNSHIP');

INSERT into public.shipping_areas values(5555, 'CLICKNSHIP', '1');
INSERT into public.shipping_areas values(4444, 'CLICKNSHIP', '2');