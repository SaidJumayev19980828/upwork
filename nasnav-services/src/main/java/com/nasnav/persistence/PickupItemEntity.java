package com.nasnav.persistence;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@DiscriminatorValue("2")
@Entity
public class PickupItemEntity extends AbstractCartItemEntity{
}
