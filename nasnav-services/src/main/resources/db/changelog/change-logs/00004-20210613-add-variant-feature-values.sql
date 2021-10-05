--liquibase formatted sql

--changeset abdelsalam:add-table-variant_feature_values dbms:postgresql splitStatements:false failOnError:true

--comment: add variant_feature_values table to store variant features values in separate table for ease querying and filtering

CREATE Table public.variant_feature_values
(
    id bigserial NOT NULL PRIMARY KEY,
    variant_id bigint NOT NULL REFERENCES public.product_variants (id),
    feature_id integer NOT NULL REFERENCES public.product_features (id),
    value character varying NOT NULL
);

insert into public.variant_feature_values (variant_id, feature_id, value)
select product_variants.id as id, (json_each(text_to_json(feature_spec))).key::int8 as feature_id, (json_each(text_to_json(feature_spec))).value::varchar as feature_value
from product_variants product_variants;

update public.variant_feature_values set value = REPLACE(value, '"', '');