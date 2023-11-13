package com.nasnav.persistence;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "service_instance")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInstanceEntity extends DefaultBusinessEntity<Long>{

    @Column(name = "package_id")
    private Long packageId;
    @Column(name = "service_id")
    private Long serviceId;
    private String name;
    private String  description;

}
