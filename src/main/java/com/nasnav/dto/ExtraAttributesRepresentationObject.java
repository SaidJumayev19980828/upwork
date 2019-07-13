package com.nasnav.dto;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)

public class ExtraAttributesRepresentationObject extends BaseRepresentationObject{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "key_name")
    private String name;
    @Column(name = "attribute_type")
    private String type;
    @Column(name = "icon")
    private String iconUrl;
}
