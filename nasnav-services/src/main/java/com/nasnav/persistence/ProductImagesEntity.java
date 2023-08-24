package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.ProductImageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Table(name="product_images")
@Data
@EqualsAndHashCode(callSuper=false)
public class ProductImagesEntity implements BaseEntity{

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @JsonIgnore
    private ProductEntity productEntity;

    @ManyToOne(fetch = FetchType.LAZY)
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
		ProductImageDTO dto = new ProductImageDTO();

		dto.setImagePath(getUri());
		dto.setProductId(getProductEntity().getId());
		if (getProductVariantsEntity() != null) {
            dto.setVariantId(getProductVariantsEntity().getId());
        }
		dto.setBarcode(getProductEntity().getBarcode());

		return dto;
	}

}
