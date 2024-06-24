--changeset Akmal:20240613-create-permission-table dbms:postgresql splitStatements:true failOnError:true

CREATE TABLE IF NOT EXISTS permission
(
    id   BIGSERIAL    NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS service_permissions
(
    service_id    BIGINT NOT NULL,
    permission_id BIGINT NOT NULL
);

ALTER TABLE service_permissions
    ADD CONSTRAINT pk_service_permissions PRIMARY KEY (service_id, permission_id);

ALTER TABLE service_permissions
    ADD CONSTRAINT fk_service_permissions_service_id
        FOREIGN KEY (service_id) REFERENCES service (id);

ALTER TABLE service_permissions
    ADD CONSTRAINT fk_service_permissions_permission_id
        FOREIGN KEY (permission_id) REFERENCES permission (id);


CREATE TABLE IF NOT EXISTS role_permissions
(
    role_id       INT NOT NULL,
    permission_id BIGINT NOT NULL
);

ALTER TABLE role_permissions
    ADD CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id);

ALTER TABLE role_permissions
    ADD CONSTRAINT fk_role_permissions_role_id
        FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE role_permissions
    ADD CONSTRAINT fk_role_permissions_permission_id
        FOREIGN KEY (permission_id) REFERENCES permission (id);