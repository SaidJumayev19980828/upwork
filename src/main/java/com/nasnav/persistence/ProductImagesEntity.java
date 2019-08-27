package com.nasnav.persistence;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="product_images")
@Data
@EqualsAndHashCode(callSuper=false)
public class ProductImagesEntity extends AbstractPersistable<Long> implements BaseEntity{

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
    @OneToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @JsonIgnore
    private ProductEntity productEntity;

    @OneToOne
    @JoinColumn(name = "variant_id", referencedColumnName = "id")
    @JsonIgnore
    private ProductVariantsEntity productVariantsEntity;

    @Column(name="type")
	private Integer type;
    @Column(name="priority")
	private Integer priority;
    @Column(name="uri")
	private String uri;
	
	
	@Override
	public BaseRepresentationObject getRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

}
