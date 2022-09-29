package com.nasnav.yeshtery.test.utils;

import com.nasnav.commons.utils.StringUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringUtilsTest {
	private List<String> validDateTime = List.of(
			"2020-01-01T00:00:00",
			"2050-12-11T23:59:59",
			"2023-01-11T23:59:59.555",
			"2023-06-31T23:00:00",
			"2023-08-02T23:00",
			"2023-08-02T23:00Z",
			"2023-08-02:23:15:12",
			"2023-08-02T23:10.854Z",
			"2023-04-18T23:00:15Z[UTC]",
			"2023-05-20T20:55:10.5549Z[UTC]",
			"2099-05-20T00:00:00Z[UTC]");

	private List<String> inValidDateTime = List.of(
			"2019-01-01T00:00:00",
			"+5555-01-01T00:00:00",
			"2022-00-01T00:00:00",
			"2022-13-01T00:00:00",
			"2022-01-32T23:59:59",
			"2022-01-30T24:00:00",
			"2022-01-30T01:60:00");

	@Test
	public void validateDateTimeTest(){
		validDateTime.forEach(dateTime -> {
			assertTrue(StringUtils.validDateTime(dateTime));
		});

		inValidDateTime.forEach(dateTime -> {
			assertFalse(StringUtils.validDateTime(dateTime));
		});
	}
}
