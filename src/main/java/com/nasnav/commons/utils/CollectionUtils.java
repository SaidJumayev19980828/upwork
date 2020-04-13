package com.nasnav.commons.utils;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import reactor.core.publisher.Flux;

public class CollectionUtils {
	public static <T> List<List<T>> divideToBatches(Collection<T> collection, int maxSize){
		List<T> nullFree = 
				collection
				.stream()
				.filter(Objects::nonNull)
				.collect(toList());
		return Flux
				.fromIterable(nullFree)
				.window(maxSize)				
				.flatMap(batch -> batch.buffer())
				.buffer()
				.single(emptyList())
				.block();
	} 
}
