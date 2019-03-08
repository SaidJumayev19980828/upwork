package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "malls")
public class MallsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;
    private String area;
    @Column(name="p_area")
    private String pArea;
    private BigDecimal lat;
    private BigDecimal lng;
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "city_id", referencedColumnName = "id")
    @JsonIgnore
    private CitiesEntity citiesEntity;

    @OneToOne(mappedBy = "mallsEntity")
    @JsonIgnore
    private ShopsEntity shopsEntity;


}
