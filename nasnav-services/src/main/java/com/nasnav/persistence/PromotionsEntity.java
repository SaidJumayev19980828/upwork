package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="promotions")
@Data
public class PromotionsEntity {
	
	public final static String MIN_AMOUNT_PROP = "cart_amount_min";
	public final static String MIN_QUANTITY_PROP = "cart_quantity_min";
	public final static String DISCOUNT_AMOUNT_MAX = "discount_value_max";
	public final static String DISCOUNT_PERCENT = "percentage";
	public final static String DISCOUNT_AMOUNT = "amount";
	public final static String ALLOWED_BRANDS = "applied_to_brands";
	public final static String ALLOWED_TAGS = "applied_to_tags";
	public final static String ALLOWED_PRODUCTS = "applied_to_products";
	public final static String ALLOWED_USERS = "applied_to_users";
	public final static String PRODUCT_QUANTITY_MIN = "product_quantity_min";
	public final static String PRODUCTS_TO_GIVE = "products_to_give";
	public final static String USAGE_LIMIT = "usage_limit";
	public final static String USAGE_LIMIT_PER_ORDER = "usage_limit_per_order";

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="identifier")
	private String identifier;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "banner")
	private String banner;

	@Column(name = "cover")
	private String cover;

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

	@Column(name = "priority")
	private Integer priority;

	@Column(name = "showing_online")
	private boolean showingOnline;

	@Column(name ="usage_limiter_count")
	private Integer usageLimiterCount;
}
