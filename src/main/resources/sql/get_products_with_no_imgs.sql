select
	variant_id,
	barcode,
	external_id,
	product_name,
	product_id
from
	(with product_mapping_type as (select
		id
	from
		integration_mapping_type imt
	where
		imt.type_name = 'PRODUCT_VARIANT' ) 
select
	variants.id as variant_id,
	variants.barcode ,
	dict.remote_value as external_id,
	product.name as product_name,
	product_no_imgs.id as product_id,
	row_number() over 
		(partition by product_no_imgs.id
			order by
				product_no_imgs.id,
				variants.id) as rownum
from
	(
	select
		prod.id as id
	from
		products prod
	where
		organization_id = ?
except (
	select
		imgs.product_id
	from
		product_images imgs
	left join files file on
		imgs.uri = file.url
	where
		file.organization_id = ?
		and imgs.product_id is not null
union
	select
		var.product_id
	from
		product_images imgs
	left join product_variants var on
		imgs.variant_id = var.id
	left join files file on
		imgs.uri = file.url
	where
		file.organization_id = ?
		and var.product_id is not null ) ) product_no_imgs
left join products product on
	product_no_imgs.id = product.id
	AND product.organization_id = ?
left join product_variants variants on
	product_no_imgs.id = variants.product_id
left join integration_mapping dict on
	variants.id::varchar = dict.local_value
	and dict.mapping_type = (select id from product_mapping_type) ) dat
where
	dat.rownum = 1