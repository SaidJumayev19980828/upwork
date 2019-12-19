package com.nasnav.persistence;

import java.math.BigDecimal;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;

import com.nasnav.dto.BasketItemDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@NamedNativeQuery(
        name = "Basket",
        query ="select b.order_id, p.id as product_id, p.name as product_name, p.p_name as product_pname, v.id as variant_id, s.id as stock_id," +
                " b.id as basket_id , b.quantity, b.price, b.currency, (select uri from product_Images where product_id = p.id and priority = 0 limit 1) " +
                "from products p join product_variants v on p.id = v.product_id join stocks s ON v.id = s.variant_id " +
                "join baskets b ON s.id = b.stock_id WHERE b.order_id in :orderId ",
        resultClass = com.nasnav.dto.BasketItemDetails.class,
        resultSetMapping = "Basket"
)
@SqlResultSetMapping(
        name = "Basket",
        classes={
                @ConstructorResult(
                        targetClass=com.nasnav.dto.BasketItemDetails.class,
                        columns={
                                @ColumnResult(name="order_id", type = Long.class),
                                @ColumnResult(name="product_id", type = Long.class),
                                @ColumnResult(name="product_name", type = String.class),
                                @ColumnResult(name="product_pname", type = String.class),
                                @ColumnResult(name="variant_id", type = Long.class),
                                @ColumnResult(name="stock_id", type = Long.class),
                                @ColumnResult(name="basket_id", type = Long.class),
                                @ColumnResult(name="quantity", type = BigDecimal.class),
                                @ColumnResult(name="price", type = BigDecimal.class),
                                @ColumnResult(name="currency", type = Integer.class),
                                @ColumnResult(name="uri", type = String.class),
                        }
                )
        }
)
@Entity
@Table(name="baskets")
@Data
@EqualsAndHashCode(callSuper=false)
public class BasketsEntity implements BaseEntity{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private BigDecimal quantity;

    @Column(name="price", precision=10, scale=2)
    private BigDecimal price;
    private Integer currency;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrdersEntity ordersEntity;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "stock_id", referencedColumnName = "id")
    @JsonIgnore
    private StocksEntity stocksEntity;

    @Override
    public BaseRepresentationObject getRepresentation() {
        return null;
    }

}
