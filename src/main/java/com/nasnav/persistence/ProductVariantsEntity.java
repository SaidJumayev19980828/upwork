package com.nasnav.persistence;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name="product_variants")
@Data
@EqualsAndHashCode(callSuper=false)
@SQLDelete(sql = "UPDATE PRODUCT_VARIANTS SET removed = 1 WHERE id = ?")
@Loader(namedQuery = "findVariantById")
@NamedQuery(name = "findVariantById", query = "SELECT v FROM ProductVariantsEntity v WHERE v.id=?1 AND v.removed = 0")
@Where(clause = "removed = 0")
public class ProductVariantsEntity {

	public ProductVariantsEntity() {
		removed = 0;
		extraAttributes = new HashSet<>();
	}
	
	
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
    
    @Column(name="removed")
    private Integer removed;
    

    @ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductEntity productEntity;
    
    
    @OneToMany(mappedBy = "productVariantsEntity")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<StocksEntity> stocks;
    
    
    @OneToMany(mappedBy = "variant", fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ProductExtraAttributesEntity> extraAttributes;
    
    
    
    public void addExtraAttribute(ProductExtraAttributesEntity extraAttribute) {
    	ProductExtraAttributesEntity extraAttr = 
    			extraAttributes
    				.stream()
    				.filter(attr -> Objects.equals(extraAttribute.getExtraAttribute(), attr.getExtraAttribute()))
    				.findFirst()
    				.orElse(extraAttribute);
    	
    	extraAttr.setValue(extraAttribute.getValue());
    	extraAttr.setVariant(this);
    	extraAttributes.add(extraAttr);
    }
    
    
    
    public void deleteExtraAttribute(ProductExtraAttributesEntity extraAttribute) {    	
    	extraAttributes.remove(extraAttribute);
    	extraAttribute.setVariant(null);
    }

}
