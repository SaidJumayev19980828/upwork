package com.nasnav.dto.request.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AbandonedCartsMail {
    private String promo;
    @JsonProperty("user_ids")
    private List<Long> userIds;
}
