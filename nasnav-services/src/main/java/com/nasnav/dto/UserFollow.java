package com.nasnav.dto;

import lombok.Data;

@Data
public class UserFollow {
    private  Long id;
    private String name;
   private Boolean isFollowing;
    private Boolean  isFollowed;
}
