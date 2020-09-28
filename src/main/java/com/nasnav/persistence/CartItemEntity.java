package com.nasnav.persistence;

import javax.persistence.*;

import lombok.Data;
import org.hibernate.annotations.DiscriminatorFormula;


@Entity
@DiscriminatorValue("0")
public class CartItemEntity extends AbstractCartItemEntity{

}
