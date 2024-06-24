package com.nasnav.dto.request;


import lombok.Data;

import java.util.List;

@Data
public class PermissionListDto {
    private List<String> permissionNames;
    private List<Integer> roleIds;
    private List<Long> serviceIds;
}
