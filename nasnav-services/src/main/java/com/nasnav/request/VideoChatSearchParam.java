package com.nasnav.request;

import com.nasnav.enumerations.VideoChatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VideoChatSearchParam extends BaseSearchParams{
    private VideoChatStatus status;
    private Boolean is_active;
    private Boolean is_assigned;
    private Boolean has_shop;
    private Long shop_id;
    private Long org_id;
    public Integer start;
    public Integer count;
}
