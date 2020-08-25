package com.nasnav.commons.utils;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Objects.isNull;

import java.math.BigDecimal;

public class MathUtils {

	public static BigDecimal calculatePercentage(BigDecimal nominator, BigDecimal denominator) {
		if(isNull(denominator) || denominator.equals(ZERO)) {
			return null;
		}
		if(isNull(nominator) || nominator.equals(ZERO)) {
			return null;
		}
		return nominator
				.divide(denominator, 10, HALF_EVEN)
				.multiply(new BigDecimal("100"));
	}
}
