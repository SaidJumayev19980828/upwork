--liquibase formatted sql

--changeset sameh-adel:create-api-call-logs-table dbms:postgresql splitStatements:false failOnError:true

--comment: create api call logs table

CREATE TABLE public.api_logs (
    id bigint NOT NULL,
    url text,
    call_date timestamp without time zone,
    customer_id bigint,
    employee_id bigint,
    organization_id bigint,
    request_content text,
    response_code int,

    CONSTRAINT api_logs_pkey PRIMARY KEY (id),
    CONSTRAINT api_logs_organization_id_fk FOREIGN KEY (organization_id) REFERENCES public.organizations(id),
    CONSTRAINT api_logs_user_id_fk FOREIGN KEY (customer_id) REFERENCES public.users(id),
    CONSTRAINT api_logs_employee_id_fk FOREIGN KEY (employee_id) REFERENCES public.employee_users(id)
);

CREATE SEQUENCE public.api_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    OWNED BY public.api_logs.id;

ALTER TABLE ONLY public.api_logs ALTER COLUMN id SET DEFAULT nextval('public.api_logs_id_seq'::regclass);