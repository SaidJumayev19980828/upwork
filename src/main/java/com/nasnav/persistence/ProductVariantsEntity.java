package com.nasnav.persistence;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="product_variants")
@Data
@EqualsAndHashCode(callSuper=false)
public class ProductVariantsEntity {


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="feature_spec")
    private String featureSpec="{}";

    @Column(name="name")
    private String name;

    @Column(name="p_name")
    private String pname;

    @Column(name="description")
    private String description;

    @Column(name="barcode")
    private String barcode;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @JsonIgnore
    private ProductEntity productEntity;

}
