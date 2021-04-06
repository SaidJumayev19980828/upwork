INSERT INTO public.countries(id,"name", iso_code, currency)VALUES(1,'Egypt', 818, 'EGP');

INSERT INTO public.cities(id,country_id, "name") VALUES(1,1, 'Cairo');
INSERT INTO public.cities(id,country_id, "name") VALUES(2,1, 'Alexandria');

INSERT INTO public.areas(id, "name", city_id)VALUES(1, 'New Cairo', 1);
INSERT INTO public.areas(id, "name", city_id)VALUES(2, 'New Cairo', 1);
INSERT INTO public.areas(id, "name", city_id)VALUES(3, 'Miami', 2);