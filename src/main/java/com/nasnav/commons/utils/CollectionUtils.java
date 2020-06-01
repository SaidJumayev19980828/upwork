package com.nasnav.commons.utils;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

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
				.collectList()
				.blockOptional()
				.orElse(emptyList());
	} 
	
	
	
	
	public static <T,U> List<U> mapInBatches(Collection<T> collection, int batchSize, Function<List<T>, List<U>> mapper){
		return divideToBatches(collection, batchSize)
				.stream()
				.map(mapper)
				.flatMap(List::stream)
				.collect(toList());
	}
	
	
	
	
	public static <T> void processInBatches(Collection<T> collection, int batchSize, Consumer<List<T>> consumer){
		divideToBatches(collection, batchSize)
		.stream()
		.forEach(consumer);
	}
	
	
	
	public static <T,U> List<T> distinctBy(Collection<T> collection, Function<? super T, ? extends U> keyExtractor){
		Set<U> seen = ConcurrentHashMap.newKeySet();
		return collection
				.stream()
				.filter(e -> distinctByKey(e, keyExtractor, seen))
				.collect(toList());
	}
	
	
	private static <T,U> boolean  distinctByKey(T element, Function<? super T, ? extends U> keyExtractor, Set<U> seen) {
		U distinctKey = keyExtractor.apply(element);
	    return seen.add(distinctKey);
	}
	
	
	
	
	@SafeVarargs
	public static <T> Set<T> setOf(T...elements){
		return new HashSet<>( asList(elements));
	}
}
