package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Data
@Entity
@DiscriminatorValue("0")
@EqualsAndHashCode(callSuper = true)
public class CartItemEntity extends AbstractCartItemEntity{

    @Column( name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
