package com.nasnav.persistence;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PreRemove;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.firestore.annotation.Exclude;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("SHOP")
@Data
@EqualsAndHashCode(callSuper = false)
public class ShopRoomTemplateEntity extends RoomTemplateEntity {

	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "shop_id", referencedColumnName = "id")
	@JsonIgnore
	@Exclude
	@lombok.ToString.Exclude
	ShopsEntity shop;
}
