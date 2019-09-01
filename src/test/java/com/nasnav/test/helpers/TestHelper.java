package com.nasnav.test.helpers;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dao.BundleRepository;
import com.nasnav.persistence.BundleEntity;
import com.nasnav.persistence.ProductEntity;

@Component 
public class TestHelper {
	
	@Autowired
	private BundleRepository bundleRepo;
	
	
	@Transactional(readOnly = true)
	public Set<ProductEntity> getBundleProductItems(Long bundleId){
		BundleEntity bundleBefore = bundleRepo.findById(bundleId).get();
		bundleBefore.getProductItems().size(); // just to fetch the elements inside the transaction
		return bundleBefore.getProductItems();
	} 

}
