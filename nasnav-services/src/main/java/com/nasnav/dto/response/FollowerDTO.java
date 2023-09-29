package com.nasnav.dto.response;

import com.nasnav.dto.UserRepresentationObject;
import lombok.Data;

@Data
public class FollowerDTO {
    private UserRepresentationObject userRepresentationObject;
    private Boolean isFollowed;
}
