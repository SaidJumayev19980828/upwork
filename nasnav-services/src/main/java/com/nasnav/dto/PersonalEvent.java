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

    String getStatus();


    @Value("#{target.employee != null ? target.employee.id : target.user != null ? target.user.id : null}")
    Long getCreatorId();

    @Value("#{target.employee != null ? 'employee' : (target.user != null ? 'user' : null)}")
    String getCreatorType();

    List<Invitees> getInvitees();

}


