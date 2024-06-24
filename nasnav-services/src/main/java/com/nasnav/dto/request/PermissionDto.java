package com.nasnav.dto.request;


import lombok.Data;

import java.util.List;

@Data
public class PermissionDto {
    private String name;
    private List<Integer> roleIds;
    private List<Long> serviceIds;
}
