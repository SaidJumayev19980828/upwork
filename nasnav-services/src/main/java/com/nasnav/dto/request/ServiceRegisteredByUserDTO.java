package com.nasnav.dto.request;

import lombok.Data;

import java.util.Date;


@Data
public class ServiceRegisteredByUserDTO {
    private Long id;
    private Long userId;
    private Long serviceId;
    private Date registeredDate;
}
