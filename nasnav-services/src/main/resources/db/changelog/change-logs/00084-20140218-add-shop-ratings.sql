CREATE TABLE shop_ratings(
                             id bigserial not null Primary Key,
                             shop_id bigint NOT NULL,
                             user_id bigint NOT NULL,
                             rate integer,
                             review text COLLATE pg_catalog."default",
                             submission_date timestamp without time zone,
                             approved boolean NOT NULL DEFAULT false
)