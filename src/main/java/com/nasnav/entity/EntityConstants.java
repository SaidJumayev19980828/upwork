package com.nasnav.entity;

public final class EntityConstants {

    static final String EMAIL_PATTEREN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    static final String Name_FIELD_PATTEREN = "^[A-Za-z_.]+$";

    public enum UserStatus {
        ACTIVE(1L),
        INACTIVE(2L),
        REQUIRE_ACTIVATION(3L);

        private Long value;

        UserStatus(Long value) {
            this.value = value;
        }

        public Long getValue() {
            return value;
        }

    }
}
