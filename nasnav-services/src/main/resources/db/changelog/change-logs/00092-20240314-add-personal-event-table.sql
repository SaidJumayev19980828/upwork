--liquibase formatted sql

--changeset Moamen:create_personal_event_tables dbms:postgresql splitStatements:true failOnError:true

--comment: ADD personal event table

CREATE TABLE personal_event (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    canceled BOOLEAN NOT NULL DEFAULT FALSE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    user_id BIGINT NULL REFERENCES users(id),
    employee_id BIGINT NULL REFERENCES employee_users(id),
    CONSTRAINT event_invitees_user_or_employee CHECK (
            (user_id IS NOT NULL AND employee_id IS NULL)
            OR (user_id IS NULL AND employee_id IS NOT NULL)
    ),
    CONSTRAINT personal_event_ends_after_starts CHECK (ends_at > starts_at)
);

CREATE TABLE event_invitees (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES personal_event(id) ON DELETE CASCADE,
    user_id BIGINT NULL REFERENCES users(id),
    employee_id BIGINT NULL REFERENCES employee_users(id),
    external_user VARCHAR(255) NULL,
    CONSTRAINT event_invitees_unique_value CHECK (
           (user_id IS NOT NULL AND employee_id IS NULL AND external_user IS NULL)
           OR (user_id IS NULL AND employee_id IS NOT NULL AND external_user IS NULL)
           OR (user_id IS NULL AND employee_id IS NULL AND external_user IS NOT NULL)
       ),
    CONSTRAINT event_invitees_unique_user UNIQUE (event_id, user_id),
    CONSTRAINT event_invitees_unique_employee UNIQUE (event_id, employee_id)
);
