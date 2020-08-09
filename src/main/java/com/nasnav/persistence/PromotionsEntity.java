package com.nasnav.persistence;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;

@Entity
@Table(name="promotions")
@Data
public class PromotionsEntity {
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="identifier")
	private String identifier;

	@ManyToOne
	@JoinColumn(name = "organization_id")
	private OrganizationEntity organization;
	
	@Column(name="date_start")
	private LocalDateTime dateStart;
	
	@Column(name="date_end")
	private LocalDateTime dateEnd;
	
	@Column(name="status")
	private Integer status;
	
	@Column(name="user_restricted")
	private Integer userRestricted;
	
	@Column(name="code")
	private String code;
	
	@Column(name="constrains")
	private String constrainsJson;
	
	@Column(name="discount")
	private String discountJson;
	
	@ManyToOne
	@JoinColumn(name="created_by")
	private EmployeeUserEntity createdBy;
	
	@Column(name="created_on")
	@CreationTimestamp
	private LocalDateTime createdOn;
}
