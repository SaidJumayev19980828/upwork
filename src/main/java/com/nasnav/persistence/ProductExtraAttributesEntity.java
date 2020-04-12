package com.nasnav.persistence;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "products_extra_attributes")
@Data
@EqualsAndHashCode(callSuper=false)
public class ProductExtraAttributesEntity {
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
	
	private String value;
	
	
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "variant_id", referencedColumnName = "id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
	private ProductVariantsEntity variant;
	
	
	
	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "extra_attribute_id", referencedColumnName = "id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
	private ExtraAttributesEntity extraAttribute;
}
