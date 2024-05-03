package com.nasnav.dto.response;

public record LikePostResponse(
        boolean showButton,
        long likeCount) {
}
