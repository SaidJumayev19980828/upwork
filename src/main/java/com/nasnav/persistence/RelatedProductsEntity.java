package com.nasnav.persistence;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="products_related")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelatedProductsEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="product_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ProductEntity product;

    @ManyToOne
    @JoinColumn(name="related_product_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ProductEntity relatedProduct;

    public RelatedProductsEntity(ProductEntity product, ProductEntity relatedProduct) {
        this.product = product;
        this.relatedProduct = relatedProduct;
    }
}
