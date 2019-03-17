package com.nasnav.persistence;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Table(name = "shops_opening_times")
@Entity
@Data
public class ShopsOpeningTimesEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "day_of_week")
	private Integer dayOfWeek;
	private Timestamp opens;
	private Timestamp closes;
	@Column(name="valid_from")
	private Date validFrom; 
	@Column(name="valid_through")
	private Date validThrough;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	@JsonIgnore
	private ShopsEntity shopsEntity;

}
