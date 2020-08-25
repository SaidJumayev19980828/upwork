package com.nasnav.commons.utils;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Objects.compare;
import static java.util.Objects.isNull;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

public class MathUtils {

	public static BigDecimal calculatePercentage(BigDecimal nominator, BigDecimal denominator) {
		if(isNull(denominator) || Objects.compare(denominator, ZERO, Comparator.naturalOrder())== 0) {
			return null;
		}
		return nominator
				.divide(denominator, 10, HALF_EVEN)
				.multiply(new BigDecimal("100"));
	}
}
