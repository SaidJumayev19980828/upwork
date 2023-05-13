package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Table(name = "services")
@Entity
@Data
public class Services implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="service_name")
    private String serviceName;

    @Column(name = "service_cost", precision = 10, scale = 2)
    private BigDecimal cost;
}
