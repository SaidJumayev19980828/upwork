package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

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
