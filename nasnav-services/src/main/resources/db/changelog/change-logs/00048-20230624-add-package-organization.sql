--liquibase formatted sql

--changeset Hussien:add_registered_package_organization_relation dbms:postgresql splitStatements:false failOnError:true

--comment: add registered package organization relation

ALTER TABLE package_registered RENAME COLUMN user_id TO creator_employee_id;

ALTER TABLE package_registered DROP CONSTRAINT package_registered_user_fk;
ALTER TABLE package_registered ADD  CONSTRAINT package_registered_employee_users_fkey FOREIGN KEY (creator_employee_id)
      REFERENCES public.employee_users (id);

ALTER TABLE package_registered ADD COLUMN if not EXISTS org_id BIGINT UNIQUE REFERENCES organizations(id);
