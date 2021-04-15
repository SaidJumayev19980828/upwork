package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

import static com.nasnav.enumerations.ProductFeatureType.STRING;

@Table(name="product_features")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class ProductFeaturesEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="p_name")
	private String pname;
	
	@Column(name="description")
	private String description;

	@Column(name="level")
	private Integer level;

	@Column(name = "type")
	private Integer type;

	@Column(name = "extra_data")
	private String extraData;

	@ManyToOne
	@ToString.Exclude
    @EqualsAndHashCode.Exclude
	private OrganizationEntity organization;


	public ProductFeaturesEntity(){
		this.type = STRING.getValue();
		this.level = 0;
	}
}
