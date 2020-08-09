package com.nasnav.enumerations;

import static com.nasnav.exceptions.ErrorCodes.ENUM$0001;
import static java.util.Arrays.stream;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.util.Objects;
import java.util.Optional;

import com.nasnav.exceptions.RuntimeBusinessException;

import lombok.Getter;

public enum PromotionStatus {
	
	INACTIVE(0),
	ACTIVE(1);
	
	
	@Getter
    private Integer value;

	PromotionStatus(Integer value) {
        this.value = value;
    }

	
	
    public static String getPromotionStatusName(Integer value) {
        return stream(values())
                 .filter(s -> Objects.equals(s.value, value))
                 .findFirst()
                 .map(PromotionStatus::name)
                 .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ENUM$0001));
    }
    
    
    
    public static Optional<PromotionStatus> getPromotionStatus(Integer value) {
        return stream(values())
                 .filter(s -> Objects.equals(s.value, value))
                 .findFirst();
    }
    
    
    
    public static Optional<PromotionStatus> getPromotionStatus(String name) {
        return stream(values())
                .filter(s -> Objects.equals(s.name(), name))
                .findFirst();
    }
}
