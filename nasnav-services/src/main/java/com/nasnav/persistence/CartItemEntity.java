package com.nasnav.persistence;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@DiscriminatorValue("0")
@EqualsAndHashCode(callSuper = true)
public class CartItemEntity extends AbstractCartItemEntity{

    @Column( name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "cartItemEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CartItemAddonDetailsEntity> addons;
    
    @Column(name="special_order")
	private String specialOrder;

}
