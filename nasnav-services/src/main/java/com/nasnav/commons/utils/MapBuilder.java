package com.nasnav.commons.utils;

import java.util.HashMap;
import java.util.Map;

import static com.nasnav.commons.utils.EntityUtils.noneIsNull;

public class MapBuilder<K,V> {
	Map<K, V> map;
	
	
	
	
	public MapBuilder() {
		this(new HashMap<>());
		
	}
	
	
	public MapBuilder(Map<K,V> map) {
		this.map = map;
	}
	
	
	
	public MapBuilder<K, V> put(K key, V value) {
		map.put(key, value);
		return this;
	}
	
	
	
	public MapBuilder<K, V> putNonNull(K key, V value) {
		if(noneIsNull(key, value)) {
			map.put(key, value);
		}		
		return this;
	} 
	
	
	
	public Map<K, V> getMap(){
		return this.map;
	}
	
	
	
	public static <K, V>  MapBuilder<K, V> map(){
		return new MapBuilder<K, V>();
	}
	
	
	

	public static <K, V>  MapBuilder<K, V> buildMap(Map<K,V> map){
		return new MapBuilder<K, V>(map);
	}
	
}
