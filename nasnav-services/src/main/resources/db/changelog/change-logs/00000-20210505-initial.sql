--liquibase formatted sql

--changeset galal:init dbms:postgresql splitStatements:false failOnError:false

--precondition-sql-check expectedResult:t SELECT count(*) < 3 FROM information_schema.tables WHERE table_schema = 'public';

--comment: the database dump @ 2021-05-05 from postgres database version 9.5.21
--comment: splitStatements=false to allow scripts with '$', so the whole script is executed as single statement
--comment: the script will run only if schema public has no tables
--comment: failOnError:false ,because this script will run against both empty databases and legacy database on our development and production environments
--comment: the precondition checks if the schema is empty except for liquibase tables.


SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: text_to_json(character varying); Type: FUNCTION; Schema: public; Owner: nasnav
--

CREATE FUNCTION public.text_to_json(character varying) RETURNS json
    LANGUAGE plpgsql IMMUTABLE
    AS $_$
  DECLARE
    x json;
  BEGIN
    BEGIN
      x := $1;
    EXCEPTION WHEN others THEN
      RETURN '{}';
    END;
    RETURN x;
  END;
$_$;


ALTER FUNCTION public.text_to_json(character varying) OWNER TO nasnav;

SET default_tablespace = '';

--
-- Name: addresses; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.addresses (
    id bigint NOT NULL,
    flat_number text,
    building_number text,
    address_line_1 text NOT NULL,
    address_line_2 text,
    latitude numeric,
    longitude numeric,
    phone_number text,
    area_id bigint,
    postal_code text,
    name text,
    first_name text,
    last_name text,
    sub_area_id bigint
);


ALTER TABLE public.addresses OWNER TO nasnav;

--
-- Name: addresses_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.addresses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.addresses_id_seq OWNER TO nasnav;

--
-- Name: addresses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.addresses_id_seq OWNED BY public.addresses.id;


--
-- Name: areas; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.areas (
    id bigint NOT NULL,
    name text,
    city_id bigint
);


ALTER TABLE public.areas OWNER TO nasnav;

--
-- Name: areas_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.areas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.areas_id_seq OWNER TO nasnav;

--
-- Name: areas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.areas_id_seq OWNED BY public.areas.id;


--
-- Name: baskets; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.baskets (
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    stock_id bigint NOT NULL,
    quantity numeric(10,2) NOT NULL,
    price numeric(10,2) NOT NULL,
    currency integer,
    discount numeric(10,2) DEFAULT 0,
    item_data text DEFAULT '{}'::text NOT NULL
);


ALTER TABLE public.baskets OWNER TO nasnav;

--
-- Name: baskets_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.baskets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.baskets_id_seq OWNER TO nasnav;

--
-- Name: baskets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.baskets_id_seq OWNED BY public.baskets.id;


--
-- Name: brands; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.brands (
    id bigint NOT NULL,
    category_id integer,
    name character varying,
    logo character varying,
    banner_image character varying,
    organization_id bigint,
    p_name character varying,
    description character varying,
    dark_logo character varying,
    removed integer DEFAULT 0 NOT NULL,
    cover_url text,
    priority integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.brands OWNER TO nasnav;

--
-- Name: brands_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.brands_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.brands_id_seq OWNER TO nasnav;

--
-- Name: brands_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.brands_id_seq OWNED BY public.brands.id;


--
-- Name: cart_items; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.cart_items (
    id bigint NOT NULL,
    stock_id bigint,
    cover_image text,
    variant_features text,
    quantity integer,
    user_id bigint,
    is_wishlist integer DEFAULT 0 NOT NULL,
    additional_data text DEFAULT '{}'::text
);


ALTER TABLE public.cart_items OWNER TO nasnav;

--
-- Name: cart_items_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.cart_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cart_items_id_seq OWNER TO nasnav;

--
-- Name: cart_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.cart_items_id_seq OWNED BY public.cart_items.id;


--
-- Name: categories; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.categories (
    id bigint NOT NULL,
    name character varying,
    parent_id integer,
    logo character varying,
    p_name character varying
);


ALTER TABLE public.categories OWNER TO nasnav;

--
-- Name: categories_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.categories_id_seq OWNER TO nasnav;

--
-- Name: categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.categories_id_seq OWNED BY public.categories.id;


--
-- Name: cities; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.cities (
    id bigint NOT NULL,
    country_id bigint,
    name character varying
);


ALTER TABLE public.cities OWNER TO nasnav;

--
-- Name: cities_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.cities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cities_id_seq OWNER TO nasnav;

--
-- Name: cities_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.cities_id_seq OWNED BY public.cities.id;


--
-- Name: countries; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.countries (
    id bigint NOT NULL,
    name character varying,
    iso_code integer NOT NULL,
    currency text NOT NULL
);


ALTER TABLE public.countries OWNER TO nasnav;

--
-- Name: countries_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.countries_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.countries_id_seq OWNER TO nasnav;

--
-- Name: countries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.countries_id_seq OWNED BY public.countries.id;


--
-- Name: employee_users; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.employee_users (
    id bigint NOT NULL,
    name character varying,
    phone_number character varying,
    email character varying DEFAULT ''::character varying NOT NULL,
    encrypted_password character varying DEFAULT ''::character varying NOT NULL,
    reset_password_token character varying,
    reset_password_sent_at timestamp without time zone,
    remember_created_at timestamp without time zone,
    sign_in_count integer DEFAULT 0 NOT NULL,
    current_sign_in_at timestamp without time zone,
    last_sign_in_at timestamp without time zone,
    current_sign_in_ip inet,
    last_sign_in_ip inet,
    avatar character varying,
    organization_id bigint,
    authentication_token character varying,
    created_by integer,
    shop_id bigint,
    organization_manager_id bigint,
    user_status integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.employee_users OWNER TO nasnav;

--
-- Name: employee_users_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.employee_users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.employee_users_id_seq OWNER TO nasnav;

--
-- Name: employee_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.employee_users_id_seq OWNED BY public.employee_users.id;


--
-- Name: extra_attributes; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.extra_attributes (
    id integer NOT NULL,
    key_name character varying,
    attribute_type character varying DEFAULT 'String'::character varying,
    organization_id bigint,
    icon character varying
);


ALTER TABLE public.extra_attributes OWNER TO nasnav;

--
-- Name: extra_attributes_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.extra_attributes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.extra_attributes_id_seq OWNER TO nasnav;

--
-- Name: extra_attributes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.extra_attributes_id_seq OWNED BY public.extra_attributes.id;


--
-- Name: files; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.files (
    id bigint NOT NULL,
    organization_id bigint,
    url text NOT NULL,
    location text NOT NULL,
    mimetype text,
    orig_filename text
);


ALTER TABLE public.files OWNER TO nasnav;

--
-- Name: files_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.files_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.files_id_seq OWNER TO nasnav;

--
-- Name: files_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.files_id_seq OWNED BY public.files.id;


--
-- Name: files_resized; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.files_resized (
    original_file_id bigint,
    width integer,
    height integer,
    image_url text,
    id bigint NOT NULL
);


ALTER TABLE public.files_resized OWNER TO nasnav;

--
-- Name: files_resized_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.files_resized_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.files_resized_id_seq OWNER TO nasnav;

--
-- Name: files_resized_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.files_resized_id_seq OWNED BY public.files_resized.id;


--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO nasnav;

--
-- Name: integration_event_failure; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.integration_event_failure (
    id bigint NOT NULL,
    organization_id bigint NOT NULL,
    event_type text NOT NULL,
    event_data text NOT NULL,
    handle_exception text NOT NULL,
    fallback_exception text,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.integration_event_failure OWNER TO nasnav;

--
-- Name: integration_event_failure_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.integration_event_failure_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.integration_event_failure_id_seq OWNER TO nasnav;

--
-- Name: integration_event_failure_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.integration_event_failure_id_seq OWNED BY public.integration_event_failure.id;


--
-- Name: integration_mapping; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.integration_mapping (
    id bigint NOT NULL,
    mapping_type bigint NOT NULL,
    local_value text NOT NULL,
    remote_value text NOT NULL,
    organization_id bigint NOT NULL
);


ALTER TABLE public.integration_mapping OWNER TO nasnav;

--
-- Name: integration_mapping_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.integration_mapping_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.integration_mapping_id_seq OWNER TO nasnav;

--
-- Name: integration_mapping_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.integration_mapping_id_seq OWNED BY public.integration_mapping.id;


--
-- Name: integration_mapping_type; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.integration_mapping_type (
    id bigint NOT NULL,
    type_name text NOT NULL
);


ALTER TABLE public.integration_mapping_type OWNER TO nasnav;

--
-- Name: integration_mapping_type_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.integration_mapping_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.integration_mapping_type_id_seq OWNER TO nasnav;

--
-- Name: integration_mapping_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.integration_mapping_type_id_seq OWNED BY public.integration_mapping_type.id;


--
-- Name: integration_param; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.integration_param (
    id bigint NOT NULL,
    param_type bigint NOT NULL,
    organization_id bigint NOT NULL,
    param_value text NOT NULL
);


ALTER TABLE public.integration_param OWNER TO nasnav;

--
-- Name: integration_param_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.integration_param_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.integration_param_id_seq OWNER TO nasnav;

--
-- Name: integration_param_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.integration_param_id_seq OWNED BY public.integration_param.id;


--
-- Name: integration_param_type; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.integration_param_type (
    id bigint NOT NULL,
    type_name text NOT NULL,
    is_mandatory boolean DEFAULT false NOT NULL
);


ALTER TABLE public.integration_param_type OWNER TO nasnav;

--
-- Name: integration_param_type_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.integration_param_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.integration_param_type_id_seq OWNER TO nasnav;

--
-- Name: integration_param_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.integration_param_type_id_seq OWNED BY public.integration_param_type.id;


--
-- Name: meta_orders; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.meta_orders (
    id bigint NOT NULL,
    created_at timestamp without time zone,
    user_id bigint,
    organization_id bigint,
    status integer DEFAULT 1 NOT NULL,
    sub_total numeric(10,2),
    shipping_total numeric(10,2),
    grand_total numeric(10,2),
    discounts numeric(10,2),
    notes text
);


ALTER TABLE public.meta_orders OWNER TO nasnav;

--
-- Name: meta_orders_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.meta_orders_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.meta_orders_id_seq OWNER TO nasnav;

--
-- Name: meta_orders_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.meta_orders_id_seq OWNED BY public.meta_orders.id;


--
-- Name: meta_orders_promotions; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.meta_orders_promotions (
    promotion bigint,
    meta_order bigint
);


ALTER TABLE public.meta_orders_promotions OWNER TO nasnav;

--
-- Name: oauth2_providers; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.oauth2_providers (
    id bigint NOT NULL,
    provider_name character varying NOT NULL
);


ALTER TABLE public.oauth2_providers OWNER TO nasnav;

--
-- Name: oauth2_providers_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.oauth2_providers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.oauth2_providers_id_seq OWNER TO nasnav;

--
-- Name: oauth2_providers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.oauth2_providers_id_seq OWNED BY public.oauth2_providers.id;


--
-- Name: oauth2_users; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.oauth2_users (
    id bigint NOT NULL,
    oauth2_id character varying NOT NULL,
    login_token character varying,
    email character varying,
    nasnav_user_id bigint,
    organization_id bigint NOT NULL,
    provider_id bigint NOT NULL
);


ALTER TABLE public.oauth2_users OWNER TO nasnav;

--
-- Name: oauth2_users_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.oauth2_users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.oauth2_users_id_seq OWNER TO nasnav;

--
-- Name: oauth2_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.oauth2_users_id_seq OWNED BY public.oauth2_users.id;


--
-- Name: orders; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.orders (
    id bigint NOT NULL,
    address character varying(150),
    name character varying(40),
    user_id bigint,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    date_delivery timestamp without time zone,
    organization_id bigint,
    status integer DEFAULT 1,
    cancelation_reasons character varying[] DEFAULT '{}'::character varying[],
    shop_id bigint,
    basket text DEFAULT '{}'::text NOT NULL,
    amount numeric(10,2) DEFAULT 0.0 NOT NULL,
    payment_status integer DEFAULT 0 NOT NULL,
    payment_id bigint,
    address_id bigint,
    meta_order_id bigint,
    total numeric(10,2) DEFAULT NULL::numeric,
    discounts numeric(10,2),
    promotion bigint
);


ALTER TABLE public.orders OWNER TO nasnav;

--
-- Name: orders_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.orders_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.orders_id_seq OWNER TO nasnav;

--
-- Name: orders_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.orders_id_seq OWNED BY public.orders.id;


--
-- Name: organization_domains; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.organization_domains (
    id bigint NOT NULL,
    domain text DEFAULT 'develop.nasnav.org'::text NOT NULL,
    organization_id bigint NOT NULL,
    subdir text,
    canonical integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.organization_domains OWNER TO nasnav;

--
-- Name: organization_domains_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.organization_domains_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_domains_id_seq OWNER TO nasnav;

--
-- Name: organization_domains_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.organization_domains_id_seq OWNED BY public.organization_domains.id;


--
-- Name: organization_image_types; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.organization_image_types (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE public.organization_image_types OWNER TO nasnav;

--
-- Name: organization_image_types_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.organization_image_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_image_types_id_seq OWNER TO nasnav;

--
-- Name: organization_image_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.organization_image_types_id_seq OWNED BY public.organization_image_types.id;


--
-- Name: organization_images; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.organization_images (
    id bigint NOT NULL,
    organization_id bigint NOT NULL,
    shop_id bigint,
    type integer DEFAULT 0 NOT NULL,
    uri text NOT NULL
);


ALTER TABLE public.organization_images OWNER TO nasnav;

--
-- Name: organization_images_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.organization_images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_images_id_seq OWNER TO nasnav;

--
-- Name: organization_images_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.organization_images_id_seq OWNED BY public.organization_images.id;


--
-- Name: organization_payments; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.organization_payments (
    id integer NOT NULL,
    organization_id bigint,
    gateway text NOT NULL,
    account text NOT NULL
);


ALTER TABLE public.organization_payments OWNER TO nasnav;

--
-- Name: organization_payments_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.organization_payments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_payments_id_seq OWNER TO nasnav;

--
-- Name: organization_payments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.organization_payments_id_seq OWNED BY public.organization_payments.id;


--
-- Name: organization_shipping_service; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.organization_shipping_service (
    shipping_service_id text NOT NULL,
    organization_id bigint NOT NULL,
    service_parameters text,
    id bigint NOT NULL
);


ALTER TABLE public.organization_shipping_service OWNER TO nasnav;

--
-- Name: organization_shipping_service_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.organization_shipping_service_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_shipping_service_id_seq OWNER TO nasnav;

--
-- Name: organization_shipping_service_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.organization_shipping_service_id_seq OWNED BY public.organization_shipping_service.id;


--
-- Name: organization_theme_classes; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.organization_theme_classes (
    id integer NOT NULL,
    organization_id bigint NOT NULL,
    theme_class_id integer NOT NULL
);


ALTER TABLE public.organization_theme_classes OWNER TO nasnav;

--
-- Name: organization_theme_classes_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.organization_theme_classes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_theme_classes_id_seq OWNER TO nasnav;

--
-- Name: organization_theme_classes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.organization_theme_classes_id_seq OWNED BY public.organization_theme_classes.id;


--
-- Name: organization_themes; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.organization_themes (
    id bigint NOT NULL,
    first_color character varying DEFAULT '#cb0226'::character varying,
    second_color character varying DEFAULT '#231f1f'::character varying,
    first_section boolean DEFAULT false,
    first_section_product integer,
    first_section_image character varying,
    logo character varying,
    second_section boolean DEFAULT false,
    second_section_product integer,
    second_section_image character varying,
    slider_body boolean DEFAULT false,
    slider_header character varying,
    slider_images character varying[] DEFAULT '{}'::character varying[],
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    organization_id bigint
);


ALTER TABLE public.organization_themes OWNER TO nasnav;

--
-- Name: organization_themes_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.organization_themes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_themes_id_seq OWNER TO nasnav;

--
-- Name: organization_themes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.organization_themes_id_seq OWNED BY public.organization_themes.id;


--
-- Name: organization_themes_settings; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.organization_themes_settings (
    id integer NOT NULL,
    organization_id bigint NOT NULL,
    theme_id integer NOT NULL,
    settings text
);


ALTER TABLE public.organization_themes_settings OWNER TO nasnav;

--
-- Name: organization_themes_settings_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.organization_themes_settings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organization_themes_settings_id_seq OWNER TO nasnav;

--
-- Name: organization_themes_settings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.organization_themes_settings_id_seq OWNED BY public.organization_themes_settings.id;


--
-- Name: organizations; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.organizations (
    id bigint NOT NULL,
    name character varying,
    description character varying,
    type character varying,
    p_name character varying,
    logo character varying,
    theme_id integer DEFAULT 0 NOT NULL,
    extra_info text,
    ecommerce integer DEFAULT 0,
    google_token text,
    currency_iso integer DEFAULT 818,
    matomo integer,
    yeshtery_state integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.organizations OWNER TO nasnav;

--
-- Name: organizations_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.organizations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organizations_id_seq OWNER TO nasnav;

--
-- Name: organizations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.organizations_id_seq OWNED BY public.organizations.id;


--
-- Name: organiztion_cart_optimization; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.organiztion_cart_optimization (
    id bigint NOT NULL,
    optimization_parameters text NOT NULL,
    optimization_strategy text NOT NULL,
    organization_id bigint NOT NULL,
    shipping_service_id text
);


ALTER TABLE public.organiztion_cart_optimization OWNER TO nasnav;

--
-- Name: organiztion_cart_optimization_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.organiztion_cart_optimization_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organiztion_cart_optimization_id_seq OWNER TO nasnav;

--
-- Name: organiztion_cart_optimization_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.organiztion_cart_optimization_id_seq OWNED BY public.organiztion_cart_optimization.id;


--
-- Name: payment_refunds; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.payment_refunds (
    id bigint NOT NULL,
    payment_id bigint NOT NULL,
    uid text NOT NULL,
    executed timestamp without time zone NOT NULL,
    amount numeric(10,2) NOT NULL,
    currency integer NOT NULL,
    status integer DEFAULT 0 NOT NULL,
    object text
);


ALTER TABLE public.payment_refunds OWNER TO nasnav;

--
-- Name: payment_refunds_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.payment_refunds_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_refunds_id_seq OWNER TO nasnav;

--
-- Name: payment_refunds_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.payment_refunds_id_seq OWNED BY public.payment_refunds.id;


--
-- Name: payments; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.payments (
    id bigint NOT NULL,
    order_id bigint,
    operator text NOT NULL,
    uid text NOT NULL,
    status integer DEFAULT 0,
    executed timestamp without time zone NOT NULL,
    amount numeric(10,2) NOT NULL,
    currency integer DEFAULT 0 NOT NULL,
    object text,
    user_id bigint,
    meta_order_id bigint,
    session_id text,
    org_payment_id integer
);


ALTER TABLE public.payments OWNER TO nasnav;

--
-- Name: payments_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.payments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payments_id_seq OWNER TO nasnav;

--
-- Name: payments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.payments_id_seq OWNED BY public.payments.id;


--
-- Name: product_bundles; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.product_bundles (
    product_id bigint NOT NULL,
    bundle_stock_id bigint NOT NULL
);


ALTER TABLE public.product_bundles OWNER TO nasnav;

--
-- Name: product_collections; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.product_collections (
    product_id bigint NOT NULL,
    variant_id bigint NOT NULL,
    priority integer,
    id bigint NOT NULL
);


ALTER TABLE public.product_collections OWNER TO nasnav;

--
-- Name: product_collections_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.product_collections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_collections_id_seq OWNER TO nasnav;

--
-- Name: product_collections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.product_collections_id_seq OWNED BY public.product_collections.id;


--
-- Name: product_features; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.product_features (
    id integer NOT NULL,
    name text NOT NULL,
    p_name text,
    description text,
    organization_id bigint NOT NULL,
    level integer DEFAULT 0 NOT NULL,
    type integer DEFAULT 0 NOT NULL,
    extra_data text DEFAULT '{}'::text
);


ALTER TABLE public.product_features OWNER TO nasnav;

--
-- Name: product_features_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.product_features_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_features_id_seq OWNER TO nasnav;

--
-- Name: product_features_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.product_features_id_seq OWNED BY public.product_features.id;


--
-- Name: product_images; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.product_images (
    id bigint NOT NULL,
    product_id bigint,
    variant_id bigint,
    type integer,
    priority integer,
    uri text NOT NULL
);


ALTER TABLE public.product_images OWNER TO nasnav;

--
-- Name: product_images_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.product_images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_images_id_seq OWNER TO nasnav;

--
-- Name: product_images_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.product_images_id_seq OWNED BY public.product_images.id;


--
-- Name: product_positions; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.product_positions (
    id bigint NOT NULL,
    shop360_id bigint,
    organization_id bigint,
    positions_json_data text,
    preview_json_data text
);


ALTER TABLE public.product_positions OWNER TO nasnav;

--
-- Name: product_positions_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.product_positions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_positions_id_seq OWNER TO nasnav;

--
-- Name: product_positions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.product_positions_id_seq OWNED BY public.product_positions.id;


--
-- Name: product_ratings; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.product_ratings (
    id bigint NOT NULL,
    variant_id bigint NOT NULL,
    user_id bigint NOT NULL,
    rate integer,
    review text,
    submission_date timestamp without time zone,
    approved boolean DEFAULT false NOT NULL
);


ALTER TABLE public.product_ratings OWNER TO nasnav;

--
-- Name: product_ratings_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.product_ratings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_ratings_id_seq OWNER TO nasnav;

--
-- Name: product_ratings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.product_ratings_id_seq OWNED BY public.product_ratings.id;


--
-- Name: product_tags; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.product_tags (
    product_id bigint NOT NULL,
    tag_id bigint NOT NULL
);


ALTER TABLE public.product_tags OWNER TO nasnav;

--
-- Name: product_variants; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.product_variants (
    id bigint NOT NULL,
    product_id bigint,
    feature_spec text DEFAULT '{}'::text NOT NULL,
    name text,
    p_name text,
    description text,
    barcode text,
    removed integer DEFAULT 0 NOT NULL,
    sku text,
    product_code text,
    weight numeric DEFAULT 0 NOT NULL
);


ALTER TABLE public.product_variants OWNER TO nasnav;

--
-- Name: product_variants_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.product_variants_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_variants_id_seq OWNER TO nasnav;

--
-- Name: product_variants_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.product_variants_id_seq OWNED BY public.product_variants.id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.products (
    id bigint NOT NULL,
    name character varying,
    p_name character varying,
    description character varying,
    brand_id bigint,
    category_id bigint,
    organization_id bigint,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hide boolean DEFAULT false NOT NULL,
    barcode text,
    product_type integer DEFAULT 0 NOT NULL,
    removed integer DEFAULT 0 NOT NULL,
    search_360 boolean DEFAULT false,
    priority integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.products OWNER TO nasnav;

--
-- Name: products_extra_attributes; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.products_extra_attributes (
    id bigint NOT NULL,
    extra_attribute_id integer,
    value character varying,
    variant_id bigint DEFAULT 1 NOT NULL
);


ALTER TABLE public.products_extra_attributes OWNER TO nasnav;

--
-- Name: products_extra_attributes_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.products_extra_attributes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.products_extra_attributes_id_seq OWNER TO nasnav;

--
-- Name: products_extra_attributes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.products_extra_attributes_id_seq OWNED BY public.products_extra_attributes.id;


--
-- Name: products_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.products_id_seq OWNER TO nasnav;

--
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.products_id_seq OWNED BY public.products.id;


--
-- Name: products_related; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.products_related (
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    related_product_id bigint NOT NULL
);


ALTER TABLE public.products_related OWNER TO nasnav;

--
-- Name: products_related_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.products_related_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.products_related_id_seq OWNER TO nasnav;

--
-- Name: products_related_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.products_related_id_seq OWNED BY public.products_related.id;


--
-- Name: promotions; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.promotions (
    id bigint NOT NULL,
    identifier text NOT NULL,
    organization_id bigint NOT NULL,
    date_start timestamp without time zone DEFAULT now() NOT NULL,
    date_end timestamp without time zone DEFAULT now() NOT NULL,
    status integer DEFAULT 0 NOT NULL,
    user_restricted integer DEFAULT 0 NOT NULL,
    code text,
    constrains text,
    discount text,
    created_by bigint,
    created_on timestamp without time zone DEFAULT now() NOT NULL,
    class_id integer DEFAULT 0 NOT NULL,
    type_id integer DEFAULT 0 NOT NULL,
    priority integer
);


ALTER TABLE public.promotions OWNER TO nasnav;

--
-- Name: promotions_cart_codes; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.promotions_cart_codes (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    code text
);


ALTER TABLE public.promotions_cart_codes OWNER TO nasnav;

--
-- Name: promotions_cart_codes_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.promotions_cart_codes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.promotions_cart_codes_id_seq OWNER TO nasnav;

--
-- Name: promotions_cart_codes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.promotions_cart_codes_id_seq OWNED BY public.promotions_cart_codes.id;


--
-- Name: promotions_codes_used; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.promotions_codes_used (
    id bigint NOT NULL,
    promotion_id bigint NOT NULL,
    user_id bigint NOT NULL,
    "time" timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.promotions_codes_used OWNER TO nasnav;

--
-- Name: promotions_codes_used_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.promotions_codes_used_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.promotions_codes_used_id_seq OWNER TO nasnav;

--
-- Name: promotions_codes_used_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.promotions_codes_used_id_seq OWNED BY public.promotions_codes_used.id;


--
-- Name: promotions_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.promotions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.promotions_id_seq OWNER TO nasnav;

--
-- Name: promotions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.promotions_id_seq OWNED BY public.promotions.id;


--
-- Name: return_request_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.return_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.return_request_id_seq OWNER TO nasnav;

--
-- Name: return_request; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.return_request (
    id bigint DEFAULT nextval('public.return_request_id_seq'::regclass) NOT NULL,
    created_on timestamp without time zone NOT NULL,
    created_by_user bigint,
    created_by_employee bigint,
    meta_order_id bigint,
    status integer DEFAULT 0
);


ALTER TABLE public.return_request OWNER TO nasnav;

--
-- Name: return_request_item_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.return_request_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.return_request_item_id_seq OWNER TO nasnav;

--
-- Name: return_request_item; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.return_request_item (
    id bigint DEFAULT nextval('public.return_request_item_id_seq'::regclass) NOT NULL,
    return_request_id bigint NOT NULL,
    order_item_id bigint,
    returned_quantity integer,
    received_quantity integer,
    received_by bigint,
    received_on timestamp without time zone,
    created_by_user bigint,
    created_by_employee bigint,
    return_shipment_id bigint
);


ALTER TABLE public.return_request_item OWNER TO nasnav;

--
-- Name: return_shipment; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.return_shipment (
    id bigint NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    status integer DEFAULT 0 NOT NULL,
    external_id text,
    track_number text,
    shipping_service_id text NOT NULL
);


ALTER TABLE public.return_shipment OWNER TO nasnav;

--
-- Name: return_shipment_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.return_shipment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.return_shipment_id_seq OWNER TO nasnav;

--
-- Name: return_shipment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.return_shipment_id_seq OWNED BY public.return_shipment.id;


--
-- Name: role_employee_users; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.role_employee_users (
    id integer NOT NULL,
    employee_user_id bigint,
    role_id integer
);


ALTER TABLE public.role_employee_users OWNER TO nasnav;

--
-- Name: role_employee_users_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.role_employee_users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.role_employee_users_id_seq OWNER TO nasnav;

--
-- Name: role_employee_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.role_employee_users_id_seq OWNED BY public.role_employee_users.id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    name character varying,
    organization_id bigint
);


ALTER TABLE public.roles OWNER TO nasnav;

--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.roles_id_seq OWNER TO nasnav;

--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: scenes; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.scenes (
    id bigint NOT NULL,
    shop_section_id bigint,
    organization_id bigint,
    name character varying,
    image character varying,
    resized text,
    thumbnail text,
    priority integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.scenes OWNER TO nasnav;

--
-- Name: scenes_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.scenes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.scenes_id_seq OWNER TO nasnav;

--
-- Name: scenes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.scenes_id_seq OWNED BY public.scenes.id;


--
-- Name: sections; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.sections (
    id bigint NOT NULL,
    shop360_id bigint,
    image character varying,
    title character varying,
    organization_id bigint
);


ALTER TABLE public.sections OWNER TO nasnav;

--
-- Name: sections_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.sections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sections_id_seq OWNER TO nasnav;

--
-- Name: sections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.sections_id_seq OWNED BY public.sections.id;


--
-- Name: seo_keywords; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.seo_keywords (
    id bigint NOT NULL,
    entity_id bigint NOT NULL,
    type_id integer NOT NULL,
    keyword text NOT NULL,
    organization_id bigint NOT NULL
);


ALTER TABLE public.seo_keywords OWNER TO nasnav;

--
-- Name: seo_keywords_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.seo_keywords_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.seo_keywords_id_seq OWNER TO nasnav;

--
-- Name: seo_keywords_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.seo_keywords_id_seq OWNED BY public.seo_keywords.id;


--
-- Name: settings; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.settings (
    id bigint NOT NULL,
    setting_name text NOT NULL,
    setting_value text NOT NULL,
    organization_id bigint,
    type integer DEFAULT 1
);


ALTER TABLE public.settings OWNER TO nasnav;

--
-- Name: settings_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.settings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.settings_id_seq OWNER TO nasnav;

--
-- Name: settings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.settings_id_seq OWNED BY public.settings.id;


--
-- Name: shipment; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.shipment (
    id bigint NOT NULL,
    sub_order_id bigint NOT NULL,
    shipping_service_id text NOT NULL,
    parameters text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    status integer DEFAULT 0 NOT NULL,
    external_id text,
    track_number text,
    shipping_fee numeric(10,2),
    delivery_from timestamp without time zone,
    delivery_until timestamp without time zone
);


ALTER TABLE public.shipment OWNER TO nasnav;

--
-- Name: shipment_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.shipment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shipment_id_seq OWNER TO nasnav;

--
-- Name: shipment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.shipment_id_seq OWNED BY public.shipment.id;


--
-- Name: shipping_areas; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.shipping_areas (
    area_id bigint NOT NULL,
    shipping_service_id text,
    provider_id text NOT NULL,
    id integer NOT NULL
);


ALTER TABLE public.shipping_areas OWNER TO nasnav;

--
-- Name: shipping_areas_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.shipping_areas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shipping_areas_id_seq OWNER TO nasnav;

--
-- Name: shipping_areas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.shipping_areas_id_seq OWNED BY public.shipping_areas.id;


--
-- Name: shipping_service; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.shipping_service (
    id text NOT NULL,
    service_parameters text,
    additional_parameters text
);


ALTER TABLE public.shipping_service OWNER TO nasnav;

--
-- Name: shop360_products; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.shop360_products (
    product_id bigint NOT NULL,
    shop_id bigint NOT NULL,
    floor_id bigint,
    scene_id bigint,
    section_id bigint,
    pitch real,
    yaw real,
    published smallint DEFAULT 1 NOT NULL,
    id bigint NOT NULL
);


ALTER TABLE public.shop360_products OWNER TO nasnav;

--
-- Name: shop360_products_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.shop360_products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shop360_products_id_seq OWNER TO nasnav;

--
-- Name: shop360_products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.shop360_products_id_seq OWNED BY public.shop360_products.id;


--
-- Name: shop360s; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.shop360s (
    id bigint NOT NULL,
    shop_id bigint,
    web_json_data text,
    url character varying,
    scene_name character varying,
    mobile_json_data text,
    published boolean DEFAULT false,
    preview_json_data text,
    deleted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.shop360s OWNER TO nasnav;

--
-- Name: shop360s_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.shop360s_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shop360s_id_seq OWNER TO nasnav;

--
-- Name: shop360s_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.shop360s_id_seq OWNED BY public.shop360s.id;


--
-- Name: shop_floors; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.shop_floors (
    id bigint NOT NULL,
    number integer,
    name character varying,
    shop360_id bigint,
    organization_id bigint
);


ALTER TABLE public.shop_floors OWNER TO nasnav;

--
-- Name: shop_floors_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.shop_floors_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shop_floors_id_seq OWNER TO nasnav;

--
-- Name: shop_floors_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.shop_floors_id_seq OWNED BY public.shop_floors.id;


--
-- Name: shop_sections; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.shop_sections (
    id bigint NOT NULL,
    shop_floor_id bigint,
    organization_id bigint,
    name character varying,
    image character varying,
    priority integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.shop_sections OWNER TO nasnav;

--
-- Name: shop_sections_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.shop_sections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shop_sections_id_seq OWNER TO nasnav;

--
-- Name: shop_sections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.shop_sections_id_seq OWNED BY public.shop_sections.id;


--
-- Name: shops; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.shops (
    id bigint NOT NULL,
    name character varying,
    phone_number character varying,
    brand_id bigint,
    organization_id bigint,
    view_image character varying,
    work_days character varying[] DEFAULT '{}'::character varying[],
    logo character varying,
    enable_logo boolean DEFAULT true,
    banner character varying,
    p_name character varying,
    address_id bigint,
    removed integer DEFAULT 0 NOT NULL,
    google_place_id text,
    is_warehouse integer DEFAULT 0 NOT NULL,
    priority integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.shops OWNER TO nasnav;

--
-- Name: shops_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.shops_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shops_id_seq OWNER TO nasnav;

--
-- Name: shops_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.shops_id_seq OWNED BY public.shops.id;


--
-- Name: shops_opening_times; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.shops_opening_times (
    shop_id bigint NOT NULL,
    day_of_week integer,
    opens time without time zone NOT NULL,
    closes time without time zone NOT NULL,
    valid_from date,
    valid_through date,
    id bigint NOT NULL
);


ALTER TABLE public.shops_opening_times OWNER TO nasnav;

--
-- Name: shops_opening_times_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.shops_opening_times_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.shops_opening_times_id_seq OWNER TO nasnav;

--
-- Name: shops_opening_times_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.shops_opening_times_id_seq OWNED BY public.shops_opening_times.id;


--
-- Name: social_links; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.social_links (
    id bigint NOT NULL,
    facebook character varying,
    twitter character varying,
    instagram character varying,
    organization_id bigint,
    linkedin text,
    youtube text,
    pinterest text
);


ALTER TABLE public.social_links OWNER TO nasnav;

--
-- Name: social_links_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.social_links_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.social_links_id_seq OWNER TO nasnav;

--
-- Name: social_links_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.social_links_id_seq OWNED BY public.social_links.id;


--
-- Name: stocks; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.stocks (
    id bigint NOT NULL,
    shop_id bigint,
    quantity integer,
    organization_id bigint,
    price numeric(10,2) DEFAULT 0,
    discount numeric(10,2) DEFAULT 0,
    variant_id bigint,
    currency integer DEFAULT 0 NOT NULL,
    unit_id integer
);


ALTER TABLE public.stocks OWNER TO nasnav;

--
-- Name: stocks_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.stocks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.stocks_id_seq OWNER TO nasnav;

--
-- Name: stocks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.stocks_id_seq OWNED BY public.stocks.id;


--
-- Name: sub_areas; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.sub_areas (
    id bigint NOT NULL,
    area_id bigint,
    organization_id bigint NOT NULL,
    name text NOT NULL,
    longitude numeric,
    latitude numeric
);


ALTER TABLE public.sub_areas OWNER TO nasnav;

--
-- Name: sub_areas_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.sub_areas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sub_areas_id_seq OWNER TO nasnav;

--
-- Name: sub_areas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.sub_areas_id_seq OWNED BY public.sub_areas.id;


--
-- Name: tag_graph_edges; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.tag_graph_edges (
    id bigint NOT NULL,
    parent_id bigint NOT NULL,
    child_id bigint NOT NULL
);


ALTER TABLE public.tag_graph_edges OWNER TO nasnav;

--
-- Name: tag_graph_edges_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.tag_graph_edges_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tag_graph_edges_id_seq OWNER TO nasnav;

--
-- Name: tag_graph_edges_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.tag_graph_edges_id_seq OWNED BY public.tag_graph_edges.id;


--
-- Name: tag_graph_nodes; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.tag_graph_nodes (
    id bigint NOT NULL,
    tag_id bigint NOT NULL
);


ALTER TABLE public.tag_graph_nodes OWNER TO nasnav;

--
-- Name: tag_graph_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.tag_graph_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tag_graph_nodes_id_seq OWNER TO nasnav;

--
-- Name: tag_graph_nodes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.tag_graph_nodes_id_seq OWNED BY public.tag_graph_nodes.id;


--
-- Name: tags; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.tags (
    id bigint NOT NULL,
    category_id bigint,
    name text NOT NULL,
    alias text,
    p_name text,
    metadata text,
    removed integer DEFAULT 0 NOT NULL,
    organization_id bigint NOT NULL,
    graph_id integer
);


ALTER TABLE public.tags OWNER TO nasnav;

--
-- Name: tags_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.tags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tags_id_seq OWNER TO nasnav;

--
-- Name: tags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.tags_id_seq OWNED BY public.tags.id;


--
-- Name: theme_classes; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.theme_classes (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE public.theme_classes OWNER TO nasnav;

--
-- Name: theme_classes_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.theme_classes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.theme_classes_id_seq OWNER TO nasnav;

--
-- Name: theme_classes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.theme_classes_id_seq OWNED BY public.theme_classes.id;


--
-- Name: themes; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.themes (
    id integer NOT NULL,
    name text NOT NULL,
    image text,
    theme_class_id integer NOT NULL,
    default_settings text,
    uid text
);


ALTER TABLE public.themes OWNER TO nasnav;

--
-- Name: themes_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.themes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.themes_id_seq OWNER TO nasnav;

--
-- Name: themes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.themes_id_seq OWNED BY public.themes.id;


--
-- Name: units; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.units (
    id integer NOT NULL,
    name text
);


ALTER TABLE public.units OWNER TO nasnav;

--
-- Name: units_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.units_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.units_id_seq OWNER TO nasnav;

--
-- Name: units_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.units_id_seq OWNED BY public.units.id;


--
-- Name: user_addresses; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.user_addresses (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    address_id bigint NOT NULL,
    principal boolean DEFAULT false NOT NULL
);


ALTER TABLE public.user_addresses OWNER TO nasnav;

--
-- Name: user_addresses_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.user_addresses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.user_addresses_id_seq OWNER TO nasnav;

--
-- Name: user_addresses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.user_addresses_id_seq OWNED BY public.user_addresses.id;


--
-- Name: user_subscriptions; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.user_subscriptions (
    id bigint NOT NULL,
    email text NOT NULL,
    organization_id bigint NOT NULL,
    token text
);


ALTER TABLE public.user_subscriptions OWNER TO nasnav;

--
-- Name: user_subscriptions_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.user_subscriptions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.user_subscriptions_id_seq OWNER TO nasnav;

--
-- Name: user_subscriptions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.user_subscriptions_id_seq OWNED BY public.user_subscriptions.id;


--
-- Name: user_tokens; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.user_tokens (
    id bigint NOT NULL,
    token text NOT NULL,
    update_time timestamp without time zone,
    user_id bigint,
    employee_user_id bigint
);


ALTER TABLE public.user_tokens OWNER TO nasnav;

--
-- Name: user_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.user_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.user_tokens_id_seq OWNER TO nasnav;

--
-- Name: user_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.user_tokens_id_seq OWNED BY public.user_tokens.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: nasnav
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email character varying DEFAULT ''::character varying NOT NULL,
    encrypted_password character varying DEFAULT ''::character varying NOT NULL,
    reset_password_token character varying,
    reset_password_sent_at timestamp without time zone,
    remember_created_at timestamp without time zone,
    sign_in_count integer DEFAULT 0 NOT NULL,
    current_sign_in_at timestamp without time zone,
    last_sign_in_at timestamp without time zone,
    current_sign_in_ip inet,
    last_sign_in_ip inet,
    user_name character varying,
    avatar character varying,
    gender character varying,
    birth_date character varying,
    authentication_token character varying DEFAULT ''::character varying,
    address character varying,
    phone_number character varying,
    post_code character varying,
    image text,
    oauth_token character varying,
    oauth_expires_at timestamp without time zone,
    organization_id bigint,
    mobile text,
    user_status integer DEFAULT 0 NOT NULL,
    first_name text,
    last_name text
);


ALTER TABLE public.users OWNER TO nasnav;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: nasnav
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO nasnav;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nasnav
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: addresses id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.addresses ALTER COLUMN id SET DEFAULT nextval('public.addresses_id_seq'::regclass);


--
-- Name: areas id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.areas ALTER COLUMN id SET DEFAULT nextval('public.areas_id_seq'::regclass);


--
-- Name: baskets id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.baskets ALTER COLUMN id SET DEFAULT nextval('public.baskets_id_seq'::regclass);


--
-- Name: brands id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.brands ALTER COLUMN id SET DEFAULT nextval('public.brands_id_seq'::regclass);


--
-- Name: cart_items id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.cart_items ALTER COLUMN id SET DEFAULT nextval('public.cart_items_id_seq'::regclass);


--
-- Name: categories id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.categories ALTER COLUMN id SET DEFAULT nextval('public.categories_id_seq'::regclass);


--
-- Name: cities id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.cities ALTER COLUMN id SET DEFAULT nextval('public.cities_id_seq'::regclass);


--
-- Name: countries id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.countries ALTER COLUMN id SET DEFAULT nextval('public.countries_id_seq'::regclass);


--
-- Name: employee_users id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.employee_users ALTER COLUMN id SET DEFAULT nextval('public.employee_users_id_seq'::regclass);


--
-- Name: extra_attributes id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.extra_attributes ALTER COLUMN id SET DEFAULT nextval('public.extra_attributes_id_seq'::regclass);


--
-- Name: files id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.files ALTER COLUMN id SET DEFAULT nextval('public.files_id_seq'::regclass);


--
-- Name: files_resized id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.files_resized ALTER COLUMN id SET DEFAULT nextval('public.files_resized_id_seq'::regclass);


--
-- Name: integration_event_failure id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_event_failure ALTER COLUMN id SET DEFAULT nextval('public.integration_event_failure_id_seq'::regclass);


--
-- Name: integration_mapping id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_mapping ALTER COLUMN id SET DEFAULT nextval('public.integration_mapping_id_seq'::regclass);


--
-- Name: integration_mapping_type id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_mapping_type ALTER COLUMN id SET DEFAULT nextval('public.integration_mapping_type_id_seq'::regclass);


--
-- Name: integration_param id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_param ALTER COLUMN id SET DEFAULT nextval('public.integration_param_id_seq'::regclass);


--
-- Name: integration_param_type id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_param_type ALTER COLUMN id SET DEFAULT nextval('public.integration_param_type_id_seq'::regclass);


--
-- Name: meta_orders id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.meta_orders ALTER COLUMN id SET DEFAULT nextval('public.meta_orders_id_seq'::regclass);


--
-- Name: oauth2_providers id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.oauth2_providers ALTER COLUMN id SET DEFAULT nextval('public.oauth2_providers_id_seq'::regclass);


--
-- Name: oauth2_users id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.oauth2_users ALTER COLUMN id SET DEFAULT nextval('public.oauth2_users_id_seq'::regclass);


--
-- Name: orders id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.orders ALTER COLUMN id SET DEFAULT nextval('public.orders_id_seq'::regclass);


--
-- Name: organization_domains id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_domains ALTER COLUMN id SET DEFAULT nextval('public.organization_domains_id_seq'::regclass);


--
-- Name: organization_images id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_images ALTER COLUMN id SET DEFAULT nextval('public.organization_images_id_seq'::regclass);


--
-- Name: organization_payments id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_payments ALTER COLUMN id SET DEFAULT nextval('public.organization_payments_id_seq'::regclass);


--
-- Name: organization_shipping_service id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_shipping_service ALTER COLUMN id SET DEFAULT nextval('public.organization_shipping_service_id_seq'::regclass);


--
-- Name: organization_theme_classes id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_theme_classes ALTER COLUMN id SET DEFAULT nextval('public.organization_theme_classes_id_seq'::regclass);


--
-- Name: organization_themes id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_themes ALTER COLUMN id SET DEFAULT nextval('public.organization_themes_id_seq'::regclass);


--
-- Name: organization_themes_settings id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_themes_settings ALTER COLUMN id SET DEFAULT nextval('public.organization_themes_settings_id_seq'::regclass);


--
-- Name: organizations id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organizations ALTER COLUMN id SET DEFAULT nextval('public.organizations_id_seq'::regclass);


--
-- Name: organiztion_cart_optimization id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organiztion_cart_optimization ALTER COLUMN id SET DEFAULT nextval('public.organiztion_cart_optimization_id_seq'::regclass);


--
-- Name: payment_refunds id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.payment_refunds ALTER COLUMN id SET DEFAULT nextval('public.payment_refunds_id_seq'::regclass);


--
-- Name: payments id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.payments ALTER COLUMN id SET DEFAULT nextval('public.payments_id_seq'::regclass);


--
-- Name: product_collections id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_collections ALTER COLUMN id SET DEFAULT nextval('public.product_collections_id_seq'::regclass);


--
-- Name: product_features id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_features ALTER COLUMN id SET DEFAULT nextval('public.product_features_id_seq'::regclass);


--
-- Name: product_images id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_images ALTER COLUMN id SET DEFAULT nextval('public.product_images_id_seq'::regclass);


--
-- Name: product_positions id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_positions ALTER COLUMN id SET DEFAULT nextval('public.product_positions_id_seq'::regclass);


--
-- Name: product_ratings id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_ratings ALTER COLUMN id SET DEFAULT nextval('public.product_ratings_id_seq'::regclass);


--
-- Name: product_variants id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_variants ALTER COLUMN id SET DEFAULT nextval('public.product_variants_id_seq'::regclass);


--
-- Name: products id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products ALTER COLUMN id SET DEFAULT nextval('public.products_id_seq'::regclass);


--
-- Name: products_extra_attributes id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products_extra_attributes ALTER COLUMN id SET DEFAULT nextval('public.products_extra_attributes_id_seq'::regclass);


--
-- Name: products_related id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products_related ALTER COLUMN id SET DEFAULT nextval('public.products_related_id_seq'::regclass);


--
-- Name: promotions id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions ALTER COLUMN id SET DEFAULT nextval('public.promotions_id_seq'::regclass);


--
-- Name: promotions_cart_codes id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions_cart_codes ALTER COLUMN id SET DEFAULT nextval('public.promotions_cart_codes_id_seq'::regclass);


--
-- Name: promotions_codes_used id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions_codes_used ALTER COLUMN id SET DEFAULT nextval('public.promotions_codes_used_id_seq'::regclass);


--
-- Name: return_shipment id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_shipment ALTER COLUMN id SET DEFAULT nextval('public.return_shipment_id_seq'::regclass);


--
-- Name: role_employee_users id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.role_employee_users ALTER COLUMN id SET DEFAULT nextval('public.role_employee_users_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: scenes id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.scenes ALTER COLUMN id SET DEFAULT nextval('public.scenes_id_seq'::regclass);


--
-- Name: sections id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.sections ALTER COLUMN id SET DEFAULT nextval('public.sections_id_seq'::regclass);


--
-- Name: seo_keywords id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.seo_keywords ALTER COLUMN id SET DEFAULT nextval('public.seo_keywords_id_seq'::regclass);


--
-- Name: settings id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.settings ALTER COLUMN id SET DEFAULT nextval('public.settings_id_seq'::regclass);


--
-- Name: shipment id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shipment ALTER COLUMN id SET DEFAULT nextval('public.shipment_id_seq'::regclass);


--
-- Name: shipping_areas id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shipping_areas ALTER COLUMN id SET DEFAULT nextval('public.shipping_areas_id_seq'::regclass);


--
-- Name: shop360_products id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360_products ALTER COLUMN id SET DEFAULT nextval('public.shop360_products_id_seq'::regclass);


--
-- Name: shop360s id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360s ALTER COLUMN id SET DEFAULT nextval('public.shop360s_id_seq'::regclass);


--
-- Name: shop_floors id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop_floors ALTER COLUMN id SET DEFAULT nextval('public.shop_floors_id_seq'::regclass);


--
-- Name: shop_sections id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop_sections ALTER COLUMN id SET DEFAULT nextval('public.shop_sections_id_seq'::regclass);


--
-- Name: shops id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shops ALTER COLUMN id SET DEFAULT nextval('public.shops_id_seq'::regclass);


--
-- Name: shops_opening_times id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shops_opening_times ALTER COLUMN id SET DEFAULT nextval('public.shops_opening_times_id_seq'::regclass);


--
-- Name: social_links id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.social_links ALTER COLUMN id SET DEFAULT nextval('public.social_links_id_seq'::regclass);


--
-- Name: stocks id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.stocks ALTER COLUMN id SET DEFAULT nextval('public.stocks_id_seq'::regclass);


--
-- Name: sub_areas id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.sub_areas ALTER COLUMN id SET DEFAULT nextval('public.sub_areas_id_seq'::regclass);


--
-- Name: tag_graph_edges id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tag_graph_edges ALTER COLUMN id SET DEFAULT nextval('public.tag_graph_edges_id_seq'::regclass);


--
-- Name: tag_graph_nodes id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tag_graph_nodes ALTER COLUMN id SET DEFAULT nextval('public.tag_graph_nodes_id_seq'::regclass);


--
-- Name: tags id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tags ALTER COLUMN id SET DEFAULT nextval('public.tags_id_seq'::regclass);


--
-- Name: theme_classes id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.theme_classes ALTER COLUMN id SET DEFAULT nextval('public.theme_classes_id_seq'::regclass);


--
-- Name: themes id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.themes ALTER COLUMN id SET DEFAULT nextval('public.themes_id_seq'::regclass);


--
-- Name: units id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.units ALTER COLUMN id SET DEFAULT nextval('public.units_id_seq'::regclass);


--
-- Name: user_addresses id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_addresses ALTER COLUMN id SET DEFAULT nextval('public.user_addresses_id_seq'::regclass);


--
-- Name: user_subscriptions id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_subscriptions ALTER COLUMN id SET DEFAULT nextval('public.user_subscriptions_id_seq'::regclass);


--
-- Name: user_tokens id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_tokens ALTER COLUMN id SET DEFAULT nextval('public.user_tokens_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: addresses addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT addresses_pkey PRIMARY KEY (id);


--
-- Name: areas areas_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.areas
    ADD CONSTRAINT areas_pkey PRIMARY KEY (id);


--
-- Name: baskets baskets_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.baskets
    ADD CONSTRAINT baskets_pkey PRIMARY KEY (id);


--
-- Name: brands brands_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.brands
    ADD CONSTRAINT brands_pkey PRIMARY KEY (id);


--
-- Name: cart_items cart_items_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_pkey PRIMARY KEY (id);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: cities cities_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.cities
    ADD CONSTRAINT cities_pkey PRIMARY KEY (id);


--
-- Name: countries countries_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.countries
    ADD CONSTRAINT countries_pkey PRIMARY KEY (id);


--
-- Name: employee_users employee_users_authentication_token_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.employee_users
    ADD CONSTRAINT employee_users_authentication_token_key UNIQUE (authentication_token);


--
-- Name: employee_users employee_users_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.employee_users
    ADD CONSTRAINT employee_users_pkey PRIMARY KEY (id);


--
-- Name: extra_attributes extra_attributes_name_unq; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.extra_attributes
    ADD CONSTRAINT extra_attributes_name_unq UNIQUE (key_name, organization_id);


--
-- Name: extra_attributes extra_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.extra_attributes
    ADD CONSTRAINT extra_attributes_pkey PRIMARY KEY (id);


--
-- Name: files files_location_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.files
    ADD CONSTRAINT files_location_key UNIQUE (location);


--
-- Name: files files_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.files
    ADD CONSTRAINT files_pkey PRIMARY KEY (id);


--
-- Name: files_resized files_resized_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.files_resized
    ADD CONSTRAINT files_resized_pkey PRIMARY KEY (id);


--
-- Name: files files_url_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.files
    ADD CONSTRAINT files_url_key UNIQUE (url);


--
-- Name: integration_event_failure integration_event_failure_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_event_failure
    ADD CONSTRAINT integration_event_failure_pkey PRIMARY KEY (id);


--
-- Name: integration_mapping integration_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_mapping
    ADD CONSTRAINT integration_mapping_pkey PRIMARY KEY (id);


--
-- Name: integration_mapping_type integration_mapping_type_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_mapping_type
    ADD CONSTRAINT integration_mapping_type_pkey PRIMARY KEY (id);


--
-- Name: integration_mapping_type integration_mapping_type_type_name_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_mapping_type
    ADD CONSTRAINT integration_mapping_type_type_name_key UNIQUE (type_name);


--
-- Name: integration_param integration_param_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_param
    ADD CONSTRAINT integration_param_pkey PRIMARY KEY (id);


--
-- Name: integration_param_type integration_param_type_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_param_type
    ADD CONSTRAINT integration_param_type_pkey PRIMARY KEY (id);


--
-- Name: integration_param_type integration_param_type_type_name_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_param_type
    ADD CONSTRAINT integration_param_type_type_name_key UNIQUE (type_name);


--
-- Name: meta_orders meta_orders_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.meta_orders
    ADD CONSTRAINT meta_orders_pkey PRIMARY KEY (id);


--
-- Name: oauth2_providers oauth2_providers_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.oauth2_providers
    ADD CONSTRAINT oauth2_providers_pkey PRIMARY KEY (id);


--
-- Name: oauth2_providers oauth2_providers_provider_name_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.oauth2_providers
    ADD CONSTRAINT oauth2_providers_provider_name_key UNIQUE (provider_name);


--
-- Name: oauth2_users oauth2_users_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.oauth2_users
    ADD CONSTRAINT oauth2_users_pkey PRIMARY KEY (id);


--
-- Name: oauth2_users oauth2_users_unq_per_org; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.oauth2_users
    ADD CONSTRAINT oauth2_users_unq_per_org UNIQUE (oauth2_id, organization_id, provider_id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: organization_domains organization_domains_domain_subdir_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_domains
    ADD CONSTRAINT organization_domains_domain_subdir_key UNIQUE (domain, subdir);


--
-- Name: organization_domains organization_domains_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_domains
    ADD CONSTRAINT organization_domains_pkey PRIMARY KEY (id);


--
-- Name: organization_image_types organization_image_types_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_image_types
    ADD CONSTRAINT organization_image_types_pkey PRIMARY KEY (id);


--
-- Name: organization_images organization_images_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_images
    ADD CONSTRAINT organization_images_pkey PRIMARY KEY (id);


--
-- Name: organization_payments organization_payments_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_payments
    ADD CONSTRAINT organization_payments_pkey PRIMARY KEY (id);


--
-- Name: organization_shipping_service organization_shipping_service_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_shipping_service
    ADD CONSTRAINT organization_shipping_service_pkey PRIMARY KEY (id);


--
-- Name: organization_theme_classes organization_theme_classes_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_theme_classes
    ADD CONSTRAINT organization_theme_classes_pkey PRIMARY KEY (id);


--
-- Name: organization_themes organization_themes_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_themes
    ADD CONSTRAINT organization_themes_pkey PRIMARY KEY (id);


--
-- Name: organization_themes_settings organization_themes_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_themes_settings
    ADD CONSTRAINT organization_themes_settings_pkey PRIMARY KEY (id);


--
-- Name: organizations organizations_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT organizations_pkey PRIMARY KEY (id);


--
-- Name: organiztion_cart_optimization organiztion_cart_optimization_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organiztion_cart_optimization
    ADD CONSTRAINT organiztion_cart_optimization_pkey PRIMARY KEY (id);


--
-- Name: payment_refunds payment_refunds_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.payment_refunds
    ADD CONSTRAINT payment_refunds_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: product_collections product_collections_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_collections
    ADD CONSTRAINT product_collections_pkey PRIMARY KEY (id);


--
-- Name: product_features product_features_name_unq; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_features
    ADD CONSTRAINT product_features_name_unq UNIQUE (name, organization_id);


--
-- Name: product_features product_features_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_features
    ADD CONSTRAINT product_features_pkey PRIMARY KEY (id);


--
-- Name: product_features product_features_pname_unq; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_features
    ADD CONSTRAINT product_features_pname_unq UNIQUE (p_name, organization_id);


--
-- Name: product_images product_images_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_images
    ADD CONSTRAINT product_images_pkey PRIMARY KEY (id);


--
-- Name: product_positions product_positions_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_positions
    ADD CONSTRAINT product_positions_pkey PRIMARY KEY (id);


--
-- Name: product_ratings product_ratings_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_ratings
    ADD CONSTRAINT product_ratings_pkey PRIMARY KEY (id);


--
-- Name: product_ratings product_ratings_variant_id_user_id_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_ratings
    ADD CONSTRAINT product_ratings_variant_id_user_id_key UNIQUE (variant_id, user_id);


--
-- Name: product_tags product_tags_product_id_tag_id_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_tags
    ADD CONSTRAINT product_tags_product_id_tag_id_key UNIQUE (product_id, tag_id);


--
-- Name: product_variants product_variants_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_variants
    ADD CONSTRAINT product_variants_pkey PRIMARY KEY (id);


--
-- Name: products_extra_attributes products_extra_attributes_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products_extra_attributes
    ADD CONSTRAINT products_extra_attributes_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: products_related products_related_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products_related
    ADD CONSTRAINT products_related_pkey PRIMARY KEY (id);


--
-- Name: products_related products_related_product_id_related_product_id_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products_related
    ADD CONSTRAINT products_related_product_id_related_product_id_key UNIQUE (product_id, related_product_id);


--
-- Name: promotions_cart_codes promotions_cart_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions_cart_codes
    ADD CONSTRAINT promotions_cart_codes_pkey PRIMARY KEY (id);


--
-- Name: promotions_codes_used promotions_codes_used_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions_codes_used
    ADD CONSTRAINT promotions_codes_used_pkey PRIMARY KEY (id);


--
-- Name: promotions promotions_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions
    ADD CONSTRAINT promotions_pkey PRIMARY KEY (id);


--
-- Name: return_request_item return_request_item_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request_item
    ADD CONSTRAINT return_request_item_pkey PRIMARY KEY (id);


--
-- Name: return_request return_request_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request
    ADD CONSTRAINT return_request_pkey PRIMARY KEY (id);


--
-- Name: return_shipment return_shipment_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_shipment
    ADD CONSTRAINT return_shipment_pkey PRIMARY KEY (id);


--
-- Name: role_employee_users role_employee_users_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.role_employee_users
    ADD CONSTRAINT role_employee_users_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: scenes scenes_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.scenes
    ADD CONSTRAINT scenes_pkey PRIMARY KEY (id);


--
-- Name: sections sections_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.sections
    ADD CONSTRAINT sections_pkey PRIMARY KEY (id);


--
-- Name: seo_keywords seo_keywords_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.seo_keywords
    ADD CONSTRAINT seo_keywords_pkey PRIMARY KEY (id);


--
-- Name: settings settings_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.settings
    ADD CONSTRAINT settings_pkey PRIMARY KEY (id);


--
-- Name: shipment shipment_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shipment
    ADD CONSTRAINT shipment_pkey PRIMARY KEY (id);


--
-- Name: shipping_areas shipping_areas_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shipping_areas
    ADD CONSTRAINT shipping_areas_pkey PRIMARY KEY (id);


--
-- Name: shipping_service shipping_service_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shipping_service
    ADD CONSTRAINT shipping_service_pkey PRIMARY KEY (id);


--
-- Name: shop360_products shop360_products_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360_products
    ADD CONSTRAINT shop360_products_pkey PRIMARY KEY (id);


--
-- Name: shop360_products shop360_products_product_id_shop_id_published_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360_products
    ADD CONSTRAINT shop360_products_product_id_shop_id_published_key UNIQUE (product_id, shop_id, published);


--
-- Name: shop360s shop360s_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360s
    ADD CONSTRAINT shop360s_pkey PRIMARY KEY (id);


--
-- Name: shop_floors shop_floors_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop_floors
    ADD CONSTRAINT shop_floors_pkey PRIMARY KEY (id);


--
-- Name: shop_sections shop_sections_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop_sections
    ADD CONSTRAINT shop_sections_pkey PRIMARY KEY (id);


--
-- Name: shops shops_name_unq; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shops
    ADD CONSTRAINT shops_name_unq UNIQUE (name, organization_id);


--
-- Name: shops_opening_times shops_opening_times_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shops_opening_times
    ADD CONSTRAINT shops_opening_times_pkey PRIMARY KEY (id);


--
-- Name: shops shops_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shops
    ADD CONSTRAINT shops_pkey PRIMARY KEY (id);


--
-- Name: social_links social_links_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.social_links
    ADD CONSTRAINT social_links_pkey PRIMARY KEY (id);


--
-- Name: stocks stocks_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.stocks
    ADD CONSTRAINT stocks_pkey PRIMARY KEY (id);


--
-- Name: stocks stocks_variant_shop_uniq; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.stocks
    ADD CONSTRAINT stocks_variant_shop_uniq UNIQUE (variant_id, shop_id);


--
-- Name: sub_areas sub_areas_name_organization_id_area_id_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.sub_areas
    ADD CONSTRAINT sub_areas_name_organization_id_area_id_key UNIQUE (name, organization_id, area_id);


--
-- Name: sub_areas sub_areas_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.sub_areas
    ADD CONSTRAINT sub_areas_pkey PRIMARY KEY (id);


--
-- Name: tag_graph_edges tag_graph_edges_parent_id_child_id_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tag_graph_edges
    ADD CONSTRAINT tag_graph_edges_parent_id_child_id_key UNIQUE (parent_id, child_id);


--
-- Name: tag_graph_edges tag_graph_edges_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tag_graph_edges
    ADD CONSTRAINT tag_graph_edges_pkey PRIMARY KEY (id);


--
-- Name: tag_graph_nodes tag_graph_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tag_graph_nodes
    ADD CONSTRAINT tag_graph_nodes_pkey PRIMARY KEY (id);


--
-- Name: tags tags_name_unq; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_name_unq UNIQUE (name, organization_id);


--
-- Name: tags tags_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_pkey PRIMARY KEY (id);


--
-- Name: tags tags_pname_unq; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_pname_unq UNIQUE (p_name, organization_id);


--
-- Name: theme_classes theme_classes_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.theme_classes
    ADD CONSTRAINT theme_classes_pkey PRIMARY KEY (id);


--
-- Name: themes themes_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.themes
    ADD CONSTRAINT themes_pkey PRIMARY KEY (id);


--
-- Name: themes themes_uid_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.themes
    ADD CONSTRAINT themes_uid_key UNIQUE (uid);


--
-- Name: units units_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.units
    ADD CONSTRAINT units_pkey PRIMARY KEY (id);


--
-- Name: integration_mapping unq_integration_map_local; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_mapping
    ADD CONSTRAINT unq_integration_map_local UNIQUE (organization_id, mapping_type, local_value);


--
-- Name: integration_mapping unq_integration_map_remote; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_mapping
    ADD CONSTRAINT unq_integration_map_remote UNIQUE (organization_id, mapping_type, remote_value);


--
-- Name: integration_param unq_integration_param; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_param
    ADD CONSTRAINT unq_integration_param UNIQUE (organization_id, param_type);


--
-- Name: user_addresses user_addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_addresses
    ADD CONSTRAINT user_addresses_pkey PRIMARY KEY (id);


--
-- Name: user_subscriptions user_subscriptions_email_organization_id_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_subscriptions
    ADD CONSTRAINT user_subscriptions_email_organization_id_key UNIQUE (email, organization_id);


--
-- Name: user_subscriptions user_subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_subscriptions
    ADD CONSTRAINT user_subscriptions_pkey PRIMARY KEY (id);


--
-- Name: user_tokens user_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_tokens
    ADD CONSTRAINT user_tokens_pkey PRIMARY KEY (id);


--
-- Name: user_tokens user_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_tokens
    ADD CONSTRAINT user_tokens_token_key UNIQUE (token);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: collection_prod_indx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX collection_prod_indx ON public.product_collections USING btree (product_id);


--
-- Name: collection_prod_unq_indx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE UNIQUE INDEX collection_prod_unq_indx ON public.product_collections USING btree (product_id, variant_id);


--
-- Name: countries_iso_code_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE UNIQUE INDEX countries_iso_code_idx ON public.countries USING btree (iso_code);


--
-- Name: files_organization_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX files_organization_id_idx ON public.files USING btree (organization_id);


--
-- Name: files_resized_original_file_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX files_resized_original_file_id_idx ON public.files_resized USING btree (original_file_id);


--
-- Name: index_brands_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_brands_on_organization_id ON public.brands USING btree (organization_id);


--
-- Name: index_cities_on_country_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_cities_on_country_id ON public.cities USING btree (country_id);


--
-- Name: index_employee_users_on_email; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE UNIQUE INDEX index_employee_users_on_email ON public.employee_users USING btree (email);


--
-- Name: index_employee_users_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_employee_users_on_organization_id ON public.employee_users USING btree (organization_id);


--
-- Name: index_employee_users_on_organization_manager_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_employee_users_on_organization_manager_id ON public.employee_users USING btree (organization_manager_id);


--
-- Name: index_employee_users_on_reset_password_token; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE UNIQUE INDEX index_employee_users_on_reset_password_token ON public.employee_users USING btree (reset_password_token);


--
-- Name: index_employee_users_on_shop_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_employee_users_on_shop_id ON public.employee_users USING btree (shop_id);


--
-- Name: index_orders_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_orders_on_organization_id ON public.orders USING btree (organization_id);


--
-- Name: index_orders_on_shop_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_orders_on_shop_id ON public.orders USING btree (shop_id);


--
-- Name: index_orders_on_user_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_orders_on_user_id ON public.orders USING btree (user_id);


--
-- Name: index_organization_themes_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_organization_themes_on_organization_id ON public.organization_themes USING btree (organization_id);


--
-- Name: index_product_positions_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_product_positions_on_organization_id ON public.product_positions USING btree (organization_id);


--
-- Name: index_product_positions_on_shop360_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_product_positions_on_shop360_id ON public.product_positions USING btree (shop360_id);


--
-- Name: index_products_on_brand_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_products_on_brand_id ON public.products USING btree (brand_id);


--
-- Name: index_products_on_category_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_products_on_category_id ON public.products USING btree (category_id);


--
-- Name: index_products_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_products_on_organization_id ON public.products USING btree (organization_id);


--
-- Name: index_roles_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_roles_on_organization_id ON public.roles USING btree (organization_id);


--
-- Name: index_scenes_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_scenes_on_organization_id ON public.scenes USING btree (organization_id);


--
-- Name: index_scenes_on_shop_section_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_scenes_on_shop_section_id ON public.scenes USING btree (shop_section_id);


--
-- Name: index_sections_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_sections_on_organization_id ON public.sections USING btree (organization_id);


--
-- Name: index_sections_on_shop360_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_sections_on_shop360_id ON public.sections USING btree (shop360_id);


--
-- Name: index_shop360s_on_shop_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_shop360s_on_shop_id ON public.shop360s USING btree (shop_id);


--
-- Name: index_shop_floors_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_shop_floors_on_organization_id ON public.shop_floors USING btree (organization_id);


--
-- Name: index_shop_floors_on_shop360_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_shop_floors_on_shop360_id ON public.shop_floors USING btree (shop360_id);


--
-- Name: index_shop_sections_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_shop_sections_on_organization_id ON public.shop_sections USING btree (organization_id);


--
-- Name: index_shop_sections_on_shop_floor_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_shop_sections_on_shop_floor_id ON public.shop_sections USING btree (shop_floor_id);


--
-- Name: index_shops_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_shops_on_organization_id ON public.shops USING btree (organization_id);


--
-- Name: index_social_links_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_social_links_on_organization_id ON public.social_links USING btree (organization_id);


--
-- Name: index_stocks_on_organization_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_stocks_on_organization_id ON public.stocks USING btree (organization_id);


--
-- Name: index_stocks_on_shop_id; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX index_stocks_on_shop_id ON public.stocks USING btree (shop_id);


--
-- Name: index_users_on_authentication_token; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE UNIQUE INDEX index_users_on_authentication_token ON public.users USING btree (authentication_token);


--
-- Name: index_users_on_reset_password_token; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE UNIQUE INDEX index_users_on_reset_password_token ON public.users USING btree (reset_password_token);


--
-- Name: organization_domains_domain_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX organization_domains_domain_idx ON public.organization_domains USING btree (domain);


--
-- Name: organiztion_cart_optimization_optimization_strategy_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE UNIQUE INDEX organiztion_cart_optimization_optimization_strategy_idx ON public.organiztion_cart_optimization USING btree (optimization_strategy, organization_id, shipping_service_id);


--
-- Name: product_images_product_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX product_images_product_id_idx ON public.product_images USING btree (product_id);


--
-- Name: product_images_uri_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX product_images_uri_idx ON public.product_images USING btree (uri);


--
-- Name: product_images_variant_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX product_images_variant_id_idx ON public.product_images USING btree (variant_id);


--
-- Name: product_tags_product_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX product_tags_product_id_idx ON public.product_tags USING btree (product_id);


--
-- Name: product_variants_product_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX product_variants_product_id_idx ON public.product_variants USING btree (product_id);


--
-- Name: product_variants_removed_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX product_variants_removed_idx ON public.product_variants USING btree (removed);


--
-- Name: products_extra_attributes_variant_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX products_extra_attributes_variant_id_idx ON public.products_extra_attributes USING btree (variant_id);


--
-- Name: products_hide_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX products_hide_idx ON public.products USING btree (hide);


--
-- Name: products_removed_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX products_removed_idx ON public.products USING btree (removed);


--
-- Name: products_removed_idx1; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX products_removed_idx1 ON public.products USING btree (removed);


--
-- Name: seo_keywords_all_indx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX seo_keywords_all_indx ON public.seo_keywords USING btree (entity_id, type_id, organization_id);


--
-- Name: seo_keywords_id_type_indx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX seo_keywords_id_type_indx ON public.seo_keywords USING btree (entity_id, type_id);


--
-- Name: seo_keywords_org_id_indx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX seo_keywords_org_id_indx ON public.seo_keywords USING btree (organization_id);


--
-- Name: settings_setting_name_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE UNIQUE INDEX settings_setting_name_idx ON public.settings USING btree (setting_name, organization_id);


--
-- Name: shops_removed_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX shops_removed_idx ON public.shops USING btree (removed);


--
-- Name: stocks_price_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX stocks_price_idx ON public.stocks USING btree (price);


--
-- Name: stocks_variant_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX stocks_variant_id_idx ON public.stocks USING btree (variant_id);


--
-- Name: tag_graph_edges_child_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX tag_graph_edges_child_id_idx ON public.tag_graph_edges USING btree (child_id);


--
-- Name: tag_graph_edges_parent_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX tag_graph_edges_parent_id_idx ON public.tag_graph_edges USING btree (parent_id);


--
-- Name: tags_graph_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX tags_graph_id_idx ON public.tags USING btree (graph_id);


--
-- Name: tags_organization_id_idx; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX tags_organization_id_idx ON public.tags USING btree (organization_id);


--
-- Name: tags_organization_id_idx1; Type: INDEX; Schema: public; Owner: nasnav
--

CREATE INDEX tags_organization_id_idx1 ON public.tags USING btree (organization_id);


--
-- Name: addresses addresses_area_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT addresses_area_id_fkey FOREIGN KEY (area_id) REFERENCES public.areas(id);


--
-- Name: addresses addresses_sub_area_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT addresses_sub_area_id_fkey FOREIGN KEY (sub_area_id) REFERENCES public.sub_areas(id);


--
-- Name: areas areas_city_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.areas
    ADD CONSTRAINT areas_city_id_fkey FOREIGN KEY (city_id) REFERENCES public.cities(id);


--
-- Name: baskets baskets_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.baskets
    ADD CONSTRAINT baskets_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: baskets baskets_stock_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.baskets
    ADD CONSTRAINT baskets_stock_id_fkey FOREIGN KEY (stock_id) REFERENCES public.stocks(id);


--
-- Name: brands brands_cover_url_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.brands
    ADD CONSTRAINT brands_cover_url_fkey FOREIGN KEY (cover_url) REFERENCES public.files(url);


--
-- Name: brands brands_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.brands
    ADD CONSTRAINT brands_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: cart_items cart_items_stock_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_stock_id_fkey FOREIGN KEY (stock_id) REFERENCES public.stocks(id);


--
-- Name: cart_items cart_items_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: employee_users employee_users_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.employee_users
    ADD CONSTRAINT employee_users_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: employee_users employee_users_shop_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.employee_users
    ADD CONSTRAINT employee_users_shop_id_fkey FOREIGN KEY (shop_id) REFERENCES public.shops(id);


--
-- Name: products_extra_attributes extra_attribute_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products_extra_attributes
    ADD CONSTRAINT extra_attribute_id_fkey FOREIGN KEY (extra_attribute_id) REFERENCES public.extra_attributes(id);


--
-- Name: extra_attributes extra_attributes_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.extra_attributes
    ADD CONSTRAINT extra_attributes_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: files files_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.files
    ADD CONSTRAINT files_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: files_resized files_resized_original_file_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.files_resized
    ADD CONSTRAINT files_resized_original_file_id_fkey FOREIGN KEY (original_file_id) REFERENCES public.files(id);


--
-- Name: shop_sections fk_rails_102545b523; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop_sections
    ADD CONSTRAINT fk_rails_102545b523 FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: shop_floors fk_rails_34316e0ca5; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop_floors
    ADD CONSTRAINT fk_rails_34316e0ca5 FOREIGN KEY (shop360_id) REFERENCES public.shop360s(id);


--
-- Name: shop_floors fk_rails_6333433b00; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop_floors
    ADD CONSTRAINT fk_rails_6333433b00 FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: organization_themes fk_rails_66b5304bc3; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_themes
    ADD CONSTRAINT fk_rails_66b5304bc3 FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: product_positions fk_rails_7a3b031e76; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_positions
    ADD CONSTRAINT fk_rails_7a3b031e76 FOREIGN KEY (shop360_id) REFERENCES public.shop360s(id);


--
-- Name: categories fk_rails_82f48f7407; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT fk_rails_82f48f7407 FOREIGN KEY (parent_id) REFERENCES public.categories(id) ON DELETE CASCADE;


--
-- Name: shop360s fk_rails_888a1fc9be; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360s
    ADD CONSTRAINT fk_rails_888a1fc9be FOREIGN KEY (shop_id) REFERENCES public.shops(id);


--
-- Name: cities fk_rails_996e05be41; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.cities
    ADD CONSTRAINT fk_rails_996e05be41 FOREIGN KEY (country_id) REFERENCES public.countries(id);


--
-- Name: social_links fk_rails_9b1a7e5d8e; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.social_links
    ADD CONSTRAINT fk_rails_9b1a7e5d8e FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: scenes fk_rails_a66b01e057; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.scenes
    ADD CONSTRAINT fk_rails_a66b01e057 FOREIGN KEY (shop_section_id) REFERENCES public.shop_sections(id);


--
-- Name: scenes fk_rails_d232c97110; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.scenes
    ADD CONSTRAINT fk_rails_d232c97110 FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: stocks fk_rails_d8eb88b3bf; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.stocks
    ADD CONSTRAINT fk_rails_d8eb88b3bf FOREIGN KEY (shop_id) REFERENCES public.shops(id);


--
-- Name: shop_sections fk_rails_f2b72e42c7; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop_sections
    ADD CONSTRAINT fk_rails_f2b72e42c7 FOREIGN KEY (shop_floor_id) REFERENCES public.shop_floors(id);


--
-- Name: orders fk_rails_f868b47f6a; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fk_rails_f868b47f6a FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: product_positions fk_rails_fefa61a65a; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_positions
    ADD CONSTRAINT fk_rails_fefa61a65a FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: integration_event_failure integration_event_failure_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_event_failure
    ADD CONSTRAINT integration_event_failure_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: integration_mapping integration_mapping_mapping_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_mapping
    ADD CONSTRAINT integration_mapping_mapping_type_fkey FOREIGN KEY (mapping_type) REFERENCES public.integration_mapping_type(id);


--
-- Name: integration_mapping integration_mapping_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_mapping
    ADD CONSTRAINT integration_mapping_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: integration_param integration_param_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_param
    ADD CONSTRAINT integration_param_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: integration_param integration_param_param_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.integration_param
    ADD CONSTRAINT integration_param_param_type_fkey FOREIGN KEY (param_type) REFERENCES public.integration_param_type(id);


--
-- Name: meta_orders_promotions meta_orders_promotions_meta_order_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.meta_orders_promotions
    ADD CONSTRAINT meta_orders_promotions_meta_order_fkey FOREIGN KEY (meta_order) REFERENCES public.meta_orders(id);


--
-- Name: meta_orders_promotions meta_orders_promotions_promotion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.meta_orders_promotions
    ADD CONSTRAINT meta_orders_promotions_promotion_fkey FOREIGN KEY (promotion) REFERENCES public.promotions(id);


--
-- Name: oauth2_users oauth2_users_nasnav_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.oauth2_users
    ADD CONSTRAINT oauth2_users_nasnav_user_id_fkey FOREIGN KEY (nasnav_user_id) REFERENCES public.users(id);


--
-- Name: oauth2_users oauth2_users_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.oauth2_users
    ADD CONSTRAINT oauth2_users_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: oauth2_users oauth2_users_provider_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.oauth2_users
    ADD CONSTRAINT oauth2_users_provider_id_fkey FOREIGN KEY (provider_id) REFERENCES public.oauth2_providers(id);


--
-- Name: orders orders_address_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_address_id_fkey FOREIGN KEY (address_id) REFERENCES public.addresses(id);


--
-- Name: orders orders_meta_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_meta_order_id_fkey FOREIGN KEY (meta_order_id) REFERENCES public.meta_orders(id);


--
-- Name: orders orders_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_payment_id_fkey FOREIGN KEY (payment_id) REFERENCES public.payments(id);


--
-- Name: orders orders_promotion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_promotion_fkey FOREIGN KEY (promotion) REFERENCES public.promotions(id);


--
-- Name: orders orders_shop_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_shop_id_fkey FOREIGN KEY (shop_id) REFERENCES public.shops(id);


--
-- Name: organization_domains organization_domains_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_domains
    ADD CONSTRAINT organization_domains_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: organization_images organization_images_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_images
    ADD CONSTRAINT organization_images_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: organization_images organization_images_shop_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_images
    ADD CONSTRAINT organization_images_shop_id_fkey FOREIGN KEY (shop_id) REFERENCES public.shops(id);


--
-- Name: organization_images organization_images_type_fk; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_images
    ADD CONSTRAINT organization_images_type_fk FOREIGN KEY (type) REFERENCES public.organization_image_types(id);


--
-- Name: organization_images organization_images_uri_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_images
    ADD CONSTRAINT organization_images_uri_fkey FOREIGN KEY (uri) REFERENCES public.files(url);


--
-- Name: organization_payments organization_payments_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_payments
    ADD CONSTRAINT organization_payments_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: organization_shipping_service organization_shipping_service_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_shipping_service
    ADD CONSTRAINT organization_shipping_service_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: organization_theme_classes organization_theme_classes_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_theme_classes
    ADD CONSTRAINT organization_theme_classes_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: organization_theme_classes organization_theme_classes_theme_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_theme_classes
    ADD CONSTRAINT organization_theme_classes_theme_class_id_fkey FOREIGN KEY (theme_class_id) REFERENCES public.theme_classes(id);


--
-- Name: organization_themes_settings organization_themes_settings_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_themes_settings
    ADD CONSTRAINT organization_themes_settings_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: organization_themes_settings organization_themes_settings_theme_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organization_themes_settings
    ADD CONSTRAINT organization_themes_settings_theme_id_fkey FOREIGN KEY (theme_id) REFERENCES public.themes(id);


--
-- Name: organizations organizations_currency_iso_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organizations
    ADD CONSTRAINT organizations_currency_iso_fkey FOREIGN KEY (currency_iso) REFERENCES public.countries(iso_code);


--
-- Name: organiztion_cart_optimization organiztion_cart_optimization_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.organiztion_cart_optimization
    ADD CONSTRAINT organiztion_cart_optimization_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: payment_refunds payment_refunds_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.payment_refunds
    ADD CONSTRAINT payment_refunds_payment_id_fkey FOREIGN KEY (payment_id) REFERENCES public.payments(id);


--
-- Name: payments payments_meta_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_meta_order_id_fkey FOREIGN KEY (meta_order_id) REFERENCES public.meta_orders(id);


--
-- Name: payments payments_org_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_org_payment_id_fkey FOREIGN KEY (org_payment_id) REFERENCES public.organization_payments(id);


--
-- Name: payments payments_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: product_bundles product_bundles_bundle_stock_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_bundles
    ADD CONSTRAINT product_bundles_bundle_stock_id_fkey FOREIGN KEY (bundle_stock_id) REFERENCES public.stocks(id);


--
-- Name: product_bundles product_bundles_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_bundles
    ADD CONSTRAINT product_bundles_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: product_collections product_collection_prod_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_collections
    ADD CONSTRAINT product_collection_prod_id_fk FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: product_collections product_collection_var_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_collections
    ADD CONSTRAINT product_collection_var_id_fk FOREIGN KEY (variant_id) REFERENCES public.product_variants(id);


--
-- Name: product_collections product_collections_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_collections
    ADD CONSTRAINT product_collections_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: product_collections product_collections_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_collections
    ADD CONSTRAINT product_collections_variant_id_fkey FOREIGN KEY (variant_id) REFERENCES public.product_variants(id);


--
-- Name: product_features product_features_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_features
    ADD CONSTRAINT product_features_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: product_images product_images_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_images
    ADD CONSTRAINT product_images_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: product_images product_images_uri_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_images
    ADD CONSTRAINT product_images_uri_fkey FOREIGN KEY (uri) REFERENCES public.files(url) ON DELETE CASCADE;


--
-- Name: product_images product_images_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_images
    ADD CONSTRAINT product_images_variant_id_fkey FOREIGN KEY (variant_id) REFERENCES public.product_variants(id);


--
-- Name: product_ratings product_ratings_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_ratings
    ADD CONSTRAINT product_ratings_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: product_ratings product_ratings_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_ratings
    ADD CONSTRAINT product_ratings_variant_id_fkey FOREIGN KEY (variant_id) REFERENCES public.product_variants(id);


--
-- Name: product_tags product_tags_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_tags
    ADD CONSTRAINT product_tags_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: product_tags product_tags_tag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_tags
    ADD CONSTRAINT product_tags_tag_id_fkey FOREIGN KEY (tag_id) REFERENCES public.tags(id);


--
-- Name: product_variants product_variants_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.product_variants
    ADD CONSTRAINT product_variants_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: products products_brand_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_brand_id_fkey FOREIGN KEY (brand_id) REFERENCES public.brands(id);


--
-- Name: products products_category_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_category_id_fk FOREIGN KEY (category_id) REFERENCES public.categories(id);


--
-- Name: products_extra_attributes products_extra_attributes_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products_extra_attributes
    ADD CONSTRAINT products_extra_attributes_variant_id_fkey FOREIGN KEY (variant_id) REFERENCES public.product_variants(id);


--
-- Name: products products_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: products_related products_related_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products_related
    ADD CONSTRAINT products_related_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: products_related products_related_related_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.products_related
    ADD CONSTRAINT products_related_related_product_id_fkey FOREIGN KEY (related_product_id) REFERENCES public.products(id);


--
-- Name: promotions_cart_codes promotions_cart_codes_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions_cart_codes
    ADD CONSTRAINT promotions_cart_codes_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: promotions_codes_used promotions_codes_used_promotion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions_codes_used
    ADD CONSTRAINT promotions_codes_used_promotion_id_fkey FOREIGN KEY (promotion_id) REFERENCES public.promotions(id);


--
-- Name: promotions_codes_used promotions_codes_used_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions_codes_used
    ADD CONSTRAINT promotions_codes_used_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: promotions promotions_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions
    ADD CONSTRAINT promotions_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.employee_users(id);


--
-- Name: promotions promotions_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.promotions
    ADD CONSTRAINT promotions_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: return_request return_request_created_by_employee_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request
    ADD CONSTRAINT return_request_created_by_employee_fkey FOREIGN KEY (created_by_employee) REFERENCES public.employee_users(id);


--
-- Name: return_request return_request_created_by_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request
    ADD CONSTRAINT return_request_created_by_user_fkey FOREIGN KEY (created_by_user) REFERENCES public.users(id);


--
-- Name: return_request_item return_request_item_created_by_employee_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request_item
    ADD CONSTRAINT return_request_item_created_by_employee_fkey FOREIGN KEY (created_by_employee) REFERENCES public.employee_users(id);


--
-- Name: return_request_item return_request_item_created_by_user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request_item
    ADD CONSTRAINT return_request_item_created_by_user_fkey FOREIGN KEY (created_by_user) REFERENCES public.users(id);


--
-- Name: return_request_item return_request_item_order_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request_item
    ADD CONSTRAINT return_request_item_order_item_id_fkey FOREIGN KEY (order_item_id) REFERENCES public.baskets(id);


--
-- Name: return_request_item return_request_item_received_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request_item
    ADD CONSTRAINT return_request_item_received_by_fkey FOREIGN KEY (received_by) REFERENCES public.employee_users(id);


--
-- Name: return_request_item return_request_item_return_request_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request_item
    ADD CONSTRAINT return_request_item_return_request_id_fkey FOREIGN KEY (return_request_id) REFERENCES public.return_request(id);


--
-- Name: return_request_item return_request_item_return_shipment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request_item
    ADD CONSTRAINT return_request_item_return_shipment_id_fkey FOREIGN KEY (return_shipment_id) REFERENCES public.return_shipment(id);


--
-- Name: return_request return_request_meta_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.return_request
    ADD CONSTRAINT return_request_meta_order_id_fkey FOREIGN KEY (meta_order_id) REFERENCES public.meta_orders(id);


--
-- Name: role_employee_users role_employee_users_employee_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.role_employee_users
    ADD CONSTRAINT role_employee_users_employee_user_id_fkey FOREIGN KEY (employee_user_id) REFERENCES public.employee_users(id) ON DELETE CASCADE;


--
-- Name: role_employee_users role_employee_users_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.role_employee_users
    ADD CONSTRAINT role_employee_users_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: sections sections_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.sections
    ADD CONSTRAINT sections_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: sections sections_shop360_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.sections
    ADD CONSTRAINT sections_shop360_id_fkey FOREIGN KEY (shop360_id) REFERENCES public.shop360s(id);


--
-- Name: seo_keywords seo_keywords_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.seo_keywords
    ADD CONSTRAINT seo_keywords_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: settings settings_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.settings
    ADD CONSTRAINT settings_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: shipment shipment_sub_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shipment
    ADD CONSTRAINT shipment_sub_order_id_fkey FOREIGN KEY (sub_order_id) REFERENCES public.orders(id);


--
-- Name: shipping_areas shipping_areas_area_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shipping_areas
    ADD CONSTRAINT shipping_areas_area_id_fkey FOREIGN KEY (area_id) REFERENCES public.areas(id);


--
-- Name: shipping_areas shipping_areas_shipping_service_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shipping_areas
    ADD CONSTRAINT shipping_areas_shipping_service_id_fkey FOREIGN KEY (shipping_service_id) REFERENCES public.shipping_service(id);


--
-- Name: shop360_products shop360_products_floor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360_products
    ADD CONSTRAINT shop360_products_floor_id_fkey FOREIGN KEY (floor_id) REFERENCES public.shop_floors(id);


--
-- Name: shop360_products shop360_products_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360_products
    ADD CONSTRAINT shop360_products_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: shop360_products shop360_products_scene_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360_products
    ADD CONSTRAINT shop360_products_scene_id_fkey FOREIGN KEY (scene_id) REFERENCES public.scenes(id);


--
-- Name: shop360_products shop360_products_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360_products
    ADD CONSTRAINT shop360_products_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.shop_sections(id);


--
-- Name: shop360_products shop360_products_shop_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shop360_products
    ADD CONSTRAINT shop360_products_shop_id_fkey FOREIGN KEY (shop_id) REFERENCES public.shops(id);


--
-- Name: shops shops_address_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shops
    ADD CONSTRAINT shops_address_id_fkey FOREIGN KEY (address_id) REFERENCES public.addresses(id);


--
-- Name: shops shops_brand_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shops
    ADD CONSTRAINT shops_brand_id_fkey FOREIGN KEY (brand_id) REFERENCES public.brands(id);


--
-- Name: shops_opening_times shops_opening_times_shop_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shops_opening_times
    ADD CONSTRAINT shops_opening_times_shop_id_fkey FOREIGN KEY (shop_id) REFERENCES public.shops(id);


--
-- Name: shops shops_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.shops
    ADD CONSTRAINT shops_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: stocks stocks_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.stocks
    ADD CONSTRAINT stocks_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: stocks stocks_unit_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.stocks
    ADD CONSTRAINT stocks_unit_id_fkey FOREIGN KEY (unit_id) REFERENCES public.units(id);


--
-- Name: stocks stocks_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.stocks
    ADD CONSTRAINT stocks_variant_id_fkey FOREIGN KEY (variant_id) REFERENCES public.product_variants(id);


--
-- Name: sub_areas sub_areas_area_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.sub_areas
    ADD CONSTRAINT sub_areas_area_id_fkey FOREIGN KEY (area_id) REFERENCES public.areas(id);


--
-- Name: sub_areas sub_areas_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.sub_areas
    ADD CONSTRAINT sub_areas_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: tag_graph_edges tag_edges_child_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tag_graph_edges
    ADD CONSTRAINT tag_edges_child_id_fkey FOREIGN KEY (child_id) REFERENCES public.tag_graph_nodes(id);


--
-- Name: tag_graph_edges tag_edges_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tag_graph_edges
    ADD CONSTRAINT tag_edges_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES public.tag_graph_nodes(id);


--
-- Name: tag_graph_nodes tag_graph_nodes_fk; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tag_graph_nodes
    ADD CONSTRAINT tag_graph_nodes_fk FOREIGN KEY (tag_id) REFERENCES public.tags(id);


--
-- Name: tags tags_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(id);


--
-- Name: tags tags_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.tags
    ADD CONSTRAINT tags_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: themes themes_theme_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.themes
    ADD CONSTRAINT themes_theme_class_id_fkey FOREIGN KEY (theme_class_id) REFERENCES public.theme_classes(id);


--
-- Name: user_addresses user_addresses_address_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_addresses
    ADD CONSTRAINT user_addresses_address_id_fkey FOREIGN KEY (address_id) REFERENCES public.addresses(id) ON DELETE CASCADE;


--
-- Name: user_addresses user_addresses_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_addresses
    ADD CONSTRAINT user_addresses_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_subscriptions user_subscriptions_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_subscriptions
    ADD CONSTRAINT user_subscriptions_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: user_tokens user_tokens_employee_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_tokens
    ADD CONSTRAINT user_tokens_employee_user_id_fkey FOREIGN KEY (employee_user_id) REFERENCES public.employee_users(id);


--
-- Name: user_tokens user_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.user_tokens
    ADD CONSTRAINT user_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: users users_organization_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nasnav
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES public.organizations(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: nasnav
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM nasnav;
GRANT ALL ON SCHEMA public TO nasnav;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--


