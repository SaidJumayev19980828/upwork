package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import com.nasnav.persistence.PackageEntity;
import io.jsonwebtoken.lang.Collections;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PackageResponse {
	private	Long id;
	private	String name;
	private	String description;
	private BigDecimal price;
	private	Integer currencyIso;
	private	String currency;
	private Long periodInDays;
	private String stripePriceId;
	private List<ServiceResponse> services;

	public static PackageResponse fromPackageEntity(PackageEntity packageEntity) {
		PackageResponse packageResponse = new PackageResponse();
		packageResponse.setId(packageEntity.getId());
		packageResponse.setName(packageEntity.getName());
		packageResponse.setDescription(packageEntity.getDescription());
		packageResponse.setPrice(packageEntity.getPrice());
		if (packageEntity.getCountry() != null) {
			packageResponse.setCurrencyIso(packageEntity.getCountry().getIsoCode());
			packageResponse.setCurrency(packageEntity.getCountry().getCurrency());
		}
		packageResponse.setPeriodInDays(packageEntity.getPeriodInDays());
		packageResponse.setStripePriceId(packageEntity.getStripePriceId());
		if (!Collections.isEmpty(packageEntity.getServices())) {
			List<ServiceResponse> serviceResponses = packageEntity.getServices().stream().map(ServiceResponse::from).toList();
			packageResponse.setServices(serviceResponses);
		}
		return packageResponse;
	}
}