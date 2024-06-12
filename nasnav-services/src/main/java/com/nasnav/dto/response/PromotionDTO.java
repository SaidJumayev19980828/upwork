package com.nasnav.dto.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.PromosConstraints;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PromotionDTO implements Comparable<PromotionDTO> {
	private Long id;
	private String identifier;
	private String name;
	private String description;
	private String banner;
	private String cover;

	private Long organizationId;
	private Integer typeId;
	private Integer classId;

	private ZonedDateTime startDate;
	private ZonedDateTime endDate;
	
	private String status;
	private String code;
	private PromosConstraints constrains;
	private Map<String,Object> discount;
	private Long userId;
	private String userName;
	private LocalDateTime createdOn;
	private Integer priority;
	private boolean showingOnline;
	private Integer usageLimiterCount;

	@Override
	public int compareTo(PromotionDTO promo) {
        if (promo != null) {
            return Comparator
                    .comparing(PromotionDTO::getPriority, Comparator.nullsFirst(Comparator.reverseOrder()))
                    .thenComparing(PromotionDTO::getId, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .compare(this, promo);
        }
        return 1;
	}
}
