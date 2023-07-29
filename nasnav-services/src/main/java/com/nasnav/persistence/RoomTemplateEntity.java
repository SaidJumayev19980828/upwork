package com.nasnav.persistence;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PreRemove;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;

@Data
@Entity
@Table(name = "room_templates")
@EqualsAndHashCode(callSuper = false)
public class RoomTemplateEntity extends DefaultBusinessEntity<Long> {

	@PreRemove
	private void preRemove() {
		shop.setRoomTemplate(null);
	}

	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "shop_id", referencedColumnName = "id")
	@JsonIgnore
	@Exclude
	@lombok.ToString.Exclude
	ShopsEntity shop;

	@Column(name = "scene_id", nullable = false)
	String sceneId;

	@Column(name = "data", nullable = false)
	String data;

	@OneToOne(mappedBy = "template", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	@Exclude
	@lombok.ToString.Exclude
	RoomSessionEntity session;
}
