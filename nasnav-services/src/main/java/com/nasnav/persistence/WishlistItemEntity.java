package com.nasnav.persistence;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@DiscriminatorValue("1")
@Entity
public class WishlistItemEntity extends AbstractCartItemEntity{
}
