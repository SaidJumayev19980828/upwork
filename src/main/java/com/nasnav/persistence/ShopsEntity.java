package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Table(name = "shops")
@Entity
@Data
public class ShopsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String country;
    private String city;
    private String zip;
    private String street;
    @Column(name = "street_number")
    private String streetNumber;
    private String floor;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "brand_id")
    private Long brandId;
    private BigDecimal lat;
    private BigDecimal lng;
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;
    @Column(name = "remote_id")
    private Long remoteId;
    @Column(name = "building_id")
    private Long buildingId;
    @Column(name = "work_times")
    private String workTimes;
    @Column(name = "view_image")
    private String viewImage;
    @Column(name = "p_street")
    private String pStreet;
    @Column(name = "time_from")
    private Timestamp timeFrom;
    @Column(name = "time_to")
    private Timestamp timeTo;

    @Type(type = "com.nasnav.persistence.GenericArrayType")
    @Column(name = "work_days")
    private String[] workDays;

    private String logo;
    @Column(name = "enable_logo")
    private Boolean enableLogo;
    private String address;
    private String banner;
    @Column(name = "p_name")
    private String pName;
    private String area;
    @Column(name = "p_area")
    private String pArea;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "mall_id", referencedColumnName = "id")
    @JsonIgnore
    private MallsEntity mallsEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private OrganizationEntity organizationEntity;


}
