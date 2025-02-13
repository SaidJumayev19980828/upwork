-- liquibase formatted sql
--changeset Moamen:web_scraping_log dbms:postgresql splitStatements:false failOnError:true

--comment: Add web_scraping_log  table

CREATE TABLE web_scraping_log (
    id bigserial PRIMARY KEY,
    log_message TEXT NOT NULL,
    http_status_code INTEGER NOT NULL,
    status_message TEXT NOT NULL,
    request_url TEXT NOT NULL,
    log_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT current_timestamp,
    org_id BIGINT NOT NULL,
    CONSTRAINT fk_org_id FOREIGN KEY (org_id) REFERENCES organizations (id) ON DELETE CASCADE
);

