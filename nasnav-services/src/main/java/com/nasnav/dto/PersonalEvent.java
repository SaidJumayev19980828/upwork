package com.nasnav.dto;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;

public interface PersonalEvent {
    Long getId();
    String getName();
    String getDescription();
    boolean getCanceled();

    LocalDateTime getStartsAt();
    LocalDateTime getEndsAt();
    @Value("#{target.employee != null ? target.employee.name : target.user != null ? target.user.name : null}")
    String getCreatorName();

    @Value("#{target.employee != null ? true : (target.user != null ? false : null)}")
    Boolean isEmployee();

    List<Invitees> getInvitees();

}


