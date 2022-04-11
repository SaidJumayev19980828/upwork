package com.nasnav.dto.response;

import com.nasnav.dto.OrganizationImagesRepresentationObject;
import com.nasnav.dto.ShopRepresentationObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class YeshteryOrganizationDTO {
    private Long id;
    private String name;
    private String description;
    List<OrganizationImagesRepresentationObject> images = new ArrayList<>();
    List<ShopRepresentationObject> shops;
    private Integer priority;
}
