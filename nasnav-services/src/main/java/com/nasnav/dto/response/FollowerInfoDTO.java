package com.nasnav.dto.response;

import lombok.Data;

@Data
public class FollowerInfoDTO {
    private Long followersCount;
    private Long followingsCount;
    private Long postsCount;
}
