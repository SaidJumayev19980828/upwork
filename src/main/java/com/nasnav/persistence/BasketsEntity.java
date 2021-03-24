package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;

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
    
    private BigDecimal discount;

    @Column(name = "item_data")
    private String itemData;

    @Override
    public BaseRepresentationObject getRepresentation() {
        return null;
    }

}
