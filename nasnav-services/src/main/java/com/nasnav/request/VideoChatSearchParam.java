package com.nasnav.request;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.VideoChatStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VideoChatSearchParam extends BaseSearchParams {
	private VideoChatStatus status;
	private Boolean isActive;
	private Boolean isAssigned;
	private Long employeeId;
	private Long userId;
	private Boolean hasShop;
	private Long shopId;
	private Long orgId;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime from;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime to;
	private Integer start;
	private Integer count;

	/* snake case setters for query params */
	@SuppressWarnings("java:S100")
	@JsonIgnore
	public void setIs_active(boolean isActive) {
		this.isActive = isActive;
	}

	@SuppressWarnings("java:S100")
	@JsonIgnore
	public void setIs_assigned(boolean isAssigned) {
		this.isAssigned = isAssigned;
	}

	@SuppressWarnings("java:S100")
	@JsonIgnore
	public void setEmployee_id(Long employeeId) {
		this.employeeId = employeeId;
	}

	@SuppressWarnings("java:S100")
	@JsonIgnore
	public void setUser_id(Long userId) {
		this.userId = userId;
	}

	@SuppressWarnings("java:S100")
	@JsonIgnore
	public void setHas_shop(boolean hasShop) {
		this.hasShop = hasShop;
	}

	@SuppressWarnings("java:S100")
	@JsonIgnore
	public void setShop_id(Long shopId) {
		this.shopId = shopId;
	}

	@SuppressWarnings("java:S100")
	@JsonIgnore
	public void setOrg_id(Long orgId) {
		this.orgId = orgId;
	}
}
