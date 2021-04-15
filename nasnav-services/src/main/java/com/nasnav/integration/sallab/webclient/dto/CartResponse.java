package com.nasnav.integration.sallab.webclient.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartResponse {

    private ObjectDescribe objectDescribe;
    private List<RecentItem> recentItems;
}
