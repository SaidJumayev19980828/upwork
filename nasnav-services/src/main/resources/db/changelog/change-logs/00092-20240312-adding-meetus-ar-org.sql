delete from public.organizations where name = 'MeetusAR' and currency_iso is null;
INSERT INTO public.organizations(name, currency_iso) VALUES ( 'MeetusAR', null);