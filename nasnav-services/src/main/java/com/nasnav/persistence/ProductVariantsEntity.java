package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static javax.persistence.GenerationType.IDENTITY;


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
        featureValues = new HashSet<>();
        weight = ZERO;
    }

    @Id
    @GeneratedValue(strategy=IDENTITY)
    private Long id;

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

    private String sku;

    @Column(name = "product_code")
    private String productCode;

    @ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
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


    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ProductExtraAttributesEntity> extraAttributes;

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<VariantFeatureValueEntity> featureValues;

    private BigDecimal weight;

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

    public void addFeatureValues(Set<VariantFeatureValueEntity> featureValues) {
        featureValues.addAll(featureValues);
    }

    public void deleteExtraAttribute(ProductExtraAttributesEntity extraAttribute) {
        extraAttributes.remove(extraAttribute);
        extraAttribute.setVariant(null);
    }

}
