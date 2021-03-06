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

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;

@Entity
@Table(name="promotions")
@Data
public class PromotionsEntity {
	
	public final static String MIN_AMOUNT_PROP = "cart_amount_min";
	public final static String DISCOUNT_AMOUNT_MAX = "discount_value_max";
	public final static String DISCOUNT_PERCENT = "percentage";
	public final static String DISCOUNT_AMOUNT = "amount";
	public final static String ALLOWED_BRANDS = "applied_to_brands";
	public final static String ALLOWED_TAGS = "applied_to_tags";
	public final static String ALLOWED_PRODUCTS = "applied_to_products";
	public final static String REQUIRED_IN_GROUP = "required";
	public final static String USE_LIMIT = "use_limit";

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="identifier")
	private String identifier;

	@ManyToOne
	@JoinColumn(name = "organization_id")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private OrganizationEntity organization;
	
	@Column(name="date_start")
	private LocalDateTime dateStart;
	
	@Column(name="date_end")
	private LocalDateTime dateEnd;
	
	@Column(name="status")
	private Integer status;
	
	@Column(name="user_restricted")
	private Integer userRestricted;

	@Column(name="class_id")
	private Integer classId;

	@Column(name="type_id")
	private Integer typeId;
	
	@Column(name="code")
	private String code;
	
	@Column(name="constrains")
	private String constrainsJson;
	
	@Column(name="discount")
	private String discountJson;
	
	@ManyToOne
	@JoinColumn(name="created_by")
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private EmployeeUserEntity createdBy;
	
	@Column(name="created_on")
	@CreationTimestamp
	private LocalDateTime createdOn;
}
