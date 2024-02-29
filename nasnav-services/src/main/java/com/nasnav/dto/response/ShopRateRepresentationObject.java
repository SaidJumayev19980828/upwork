package com.nasnav.dto.response;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class ShopRateRepresentationObject extends BaseRepresentationObject {
    private Long id;
    private Long shoptId;
    private String review;
    private Integer rate;
    private Long userId;
    private String userName;
    private LocalDateTime submissionDate;
    private boolean approved;
}
