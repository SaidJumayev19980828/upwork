package com.nasnav.dto;

public interface UserListFollowProjection {
    UserProjection getUser();
    Boolean getIsFollowing();
    Boolean getIsFollowed();
}
