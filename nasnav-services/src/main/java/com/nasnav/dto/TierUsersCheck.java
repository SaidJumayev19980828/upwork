package com.nasnav.dto;

import lombok.Data;

import java.util.List;

@Data
public class TierUsersCheck {
    private Boolean softDelete;
    private List<UserRepresentationObject> users;
}
