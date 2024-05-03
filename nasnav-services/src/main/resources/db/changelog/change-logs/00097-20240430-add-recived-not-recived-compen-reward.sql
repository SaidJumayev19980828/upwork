--liquibase formatted sql

--changeset Moamen:add-reward-table dbms:postgresql splitStatements:true failOnError:true

CREATE TABLE eligible_not_received (
    id bigserial not null Primary Key,
    user_id bigint not null References users(id),
    eligibility_date DATE NOT NULL,
    sub_post_id BIGINT,
    compensation_tier BIGINT not null References compensation_rule_tier(id),
    eligible_amount DECIMAL(10, 2),
    reason_for_eligibility VARCHAR(100), -- Reason the person is eligible (optional)
    org_id BIGINT not null References organizations(id)
);

ALTER TABLE eligible_not_received ADD CONSTRAINT FK_eligible_not_received_ON_POST FOREIGN KEY (sub_post_id)  REFERENCES sub_posts (id);


CREATE TABLE received_award (
    id bigserial not null Primary Key,
    user_id bigint not null References users(id),
    award_date DATE NOT NULL, -- Date the award was received (required)
    sub_post_id BIGINT, -- The specific tier or post (required)
    compensation_tier BIGINT not null References compensation_rule_tier(id),
    award_description VARCHAR(100) NOT NULL, -- Description of the award (required)
    award_amount DECIMAL(10, 2), -- Amount of the award (optional)
    org_id BIGINT not null References organizations(id)

);

ALTER TABLE received_award ADD CONSTRAINT FK_received_award_ON_POST FOREIGN KEY (sub_post_id)  REFERENCES sub_posts (id);