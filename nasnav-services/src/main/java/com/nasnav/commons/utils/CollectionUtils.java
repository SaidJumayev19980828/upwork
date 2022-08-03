package com.nasnav.commons.utils;

import org.json.JSONArray;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

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

	public static Stream<Object> streamJsonArrayElements(String jsonString){
		return ofNullable(jsonString)
				.map(JSONArray::new)
				.map(JSONArray::spliterator)
				.map(iterator -> StreamSupport.stream(iterator, false))
				.orElse(Stream.empty());
	}
	
	
	@SafeVarargs
	public static <T> Set<T> setOf(T...elements){
		return new HashSet<>( asList(elements));
	}



    public static <T> List<T> concat(Collection<T> collection1, Collection<T> collection2) {
		return Stream
				.concat(collection1.stream(), collection2.stream())
				.collect(toList());
    }

	public static <T> boolean listsEqualsIgnoreOrder(List<T> list1, List<T> list2) {
		if (list1 == null)
			return list2==null;
		if (list2 == null)
			return list1 == null;

		return new HashSet<>(list1).equals(new HashSet<>(list2));
	}
}
