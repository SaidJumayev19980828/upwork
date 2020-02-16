package com.nasnav.dto;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;

public class ResponsePageable extends Pageable {
	
	public ResponsePageable() {
		
	}
	
	
	public ResponsePageable(Pageable pageable) {
		try {
			BeanUtils.copyProperties(this, pageable);
		} catch (Throwable e) {
			e.printStackTrace();
		} 
	}
}
