package com.nasnav.test.utils;

import com.nasnav.commons.utils.MapBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.nasnav.commons.utils.CollectionUtils.distinctBy;
import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

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
	
	
	
	
	@Test
	public void testCollectionDistinctBy() {
		List<TestDataClass> input = 
				asList(new TestDataClass(1,"str1"), new TestDataClass(2,"str1"), new TestDataClass(3,"str1"));
		
		List<TestDataClass> normalDistinct = input.stream().distinct().collect(toList());
		List<TestDataClass> output = distinctBy(input, TestDataClass::getCharacters);
		
		
		assertEquals("normal distinct will give 3 items, as each has different num", input.size() ,normalDistinct.size());
		assertEquals("as the distinct is by characters field, only first object will be inserted", 1, output.size());
	}



	@Test
	public void testMapInBatches(){
		int size = 100;
		int batchSize = 20;
		List<Integer> testIntegers = IntStream. range(0,size).boxed().collect(toList());

		List<Integer> result =
				mapInBatches(testIntegers, batchSize, batch -> assertBatchSize(batchSize, batch))
				 .stream()
				 .map(i -> i+1)
				.collect(toList());
		assertEquals(size, result.size());
	}



	private List<Integer> assertBatchSize(int batchSize, List<Integer> batch) {
		assertEquals(batchSize, batch.size());
		return batch;
	}

}



@Data
@AllArgsConstructor
class TestDataClass{
	private Integer num;
	private String characters;
}
