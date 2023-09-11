package com.nasnav.persistence;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DiscriminatorFormula;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;

@Data
@Entity
@Table(name = "room_templates")
@EqualsAndHashCode(callSuper = false)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("case when shop_id is not null then 'SHOP' when event_id is not null then 'EVENT' else null end")
public class RoomTemplateEntity extends DefaultBusinessEntity<Long> {

	@Column(name = "scene_id", nullable = false)
	String sceneId;

	@Column(name = "data", nullable = false)
	String data;

	@OneToOne(mappedBy = "template", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	@Exclude
	@lombok.ToString.Exclude
	RoomSessionEntity session;

	public Boolean isStarted()  {
		return session != null;
	}
}
