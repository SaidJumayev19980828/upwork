package com.nasnav.test.utils;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.nasnav.commons.utils.MapBuilder;

public class UtilsTest {
	
	@Test
	public void  testMapBuilder() {
		String nullStr = null;
		Map<String,String> map = 
			MapBuilder
			.<String,String>map()
			.put("test", "val")
			.putNonNull(nullStr, "shoudln't exist")
			.putNonNull("shoudln't exist", nullStr)
			.getMap();
		
		assertEquals(1, map.keySet().size());
		assertEquals("test", map.keySet().stream().findFirst().get());
	}
}
