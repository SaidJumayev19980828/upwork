package com.nasnav.commons.utils;

import java.util.Collection;
import java.util.List;

import reactor.core.publisher.Flux;

public class CollectionUtils {
	public static <T> List<List<T>> divideToBatches(Collection<T> collection, int maxSize){
		return Flux
				.fromIterable(collection)
				.window(maxSize)				
				.flatMap(batch -> batch.buffer())
				.buffer()
				.single()
				.block();
	} 
}
