ALTER TABLE public.call_queue ADD shop_id BIGINT  NULL References public.shops(id) ;
