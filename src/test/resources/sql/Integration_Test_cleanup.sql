DELETE FROM  public.integration_event_failure WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.integration_mapping WHERE organization_id BETWEEN 99000 AND 99999;
DELETE FROM public.integration_mapping_type;
--delete from public.integration_param where param_type = 6600004;
--delete from public.integration_param_type where id = 6600004;
