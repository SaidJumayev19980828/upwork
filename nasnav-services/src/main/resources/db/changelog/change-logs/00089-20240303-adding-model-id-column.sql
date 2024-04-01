ALTER TABLE public.files add column IF NOT EXISTS model_id bigint;
ALTER TABLE public.products add column IF NOT EXISTS model_id bigint;