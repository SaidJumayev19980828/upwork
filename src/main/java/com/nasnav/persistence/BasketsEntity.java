package com.nasnav.persistence;

import java.math.BigDecimal;

import javax.persistence.*;

import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="baskets")
@Data
@EqualsAndHashCode(callSuper=false)
public class BasketsEntity extends AbstractPersistable<Long> implements BaseEntity{

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

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "stock_id", referencedColumnName = "id")
    @JsonIgnore
    private StocksEntity stocksEntity;

    @Override
    public BaseRepresentationObject getRepresentation() {
        return null;
    }

}
