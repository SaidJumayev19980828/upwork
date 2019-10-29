package com.nasnav.persistence;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
