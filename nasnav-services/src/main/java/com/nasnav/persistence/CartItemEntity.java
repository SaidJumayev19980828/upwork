package com.nasnav.persistence;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue("0")
public class CartItemEntity extends AbstractCartItemEntity{

}
