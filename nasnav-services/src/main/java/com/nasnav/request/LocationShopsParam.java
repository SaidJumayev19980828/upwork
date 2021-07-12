package com.nasnav.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationShopsParam {
    private String name;
    private Long orgId;
    private Long areaId;
    private Double minLongitude;
    private Double minLatitude;
    private Double maxLongitude;
    private Double maxLatitude;
    private Double longitude;
    private Double latitude;
    private Double radius;
    private boolean yeshteryState;
    private boolean searchInTags;
    private Integer[] productType;
}
