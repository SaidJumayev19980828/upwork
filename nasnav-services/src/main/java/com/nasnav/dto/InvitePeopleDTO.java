package com.nasnav.dto;

import lombok.Data;

@Data
public class InvitePeopleDTO {
    private Long user;
    private boolean employee;
    private String externalMail;
}
