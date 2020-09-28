package com.nasnav.persistence;


import javax.persistence.*;

@DiscriminatorValue("1")
@Entity
public class WishlistItemEntity extends AbstractCartItemEntity{
}
