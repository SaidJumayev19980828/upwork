package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class OrganizationServicesDto {
    private Long orgId;
    private Long serviceId;
    private Boolean enabled;
}
