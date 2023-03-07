package com.nasnav.enumerations;

import static com.nasnav.exceptions.ErrorCodes.ORG$ADDON$0001;
import static java.util.Arrays.stream;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nasnav.exceptions.RuntimeBusinessException;

import lombok.Getter;

public enum AddonType {

	
EXTRA(1), LEAVOUT(0);
	
	@Getter
	@JsonValue
    private final int value;
	
	@JsonCreator
	AddonType(int value) {
        this.value = value;
    }
	
	
	public static AddonType getAddonsType(Integer value) {
        return stream(values())
                .filter(s -> Objects.equals(s.value, value))
                .findFirst()
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$ADDON$0001, value));
    }
}
