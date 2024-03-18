package com.nasnav.dto;

import org.springframework.beans.factory.annotation.Value;

public interface Invitees {
    @Value("#{target.employee != null ? target.employee.name : target.user != null ? target.user.name : target.externalUser}")
    String getInviteeName();

    @Value("#{target.employee != null ? true : (target.user != null ? false : false)}")
    Boolean isEmployee();
}
