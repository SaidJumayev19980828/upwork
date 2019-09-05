package com.nasnav.test.helpers;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dao.BundleRepository;
import com.nasnav.persistence.BundleEntity;
import com.nasnav.persistence.StocksEntity;

@Component 
public class TestHelper {
	
	@Autowired
	private BundleRepository bundleRepo;
	
	
	@Transactional(readOnly = true)
	public Set<StocksEntity> getBundleItems(Long bundleId){
		BundleEntity bundle = bundleRepo.findById(bundleId).get();
		bundle.getItems().size(); // just to force fetching the elements inside this transaction
		return bundle.getItems();
	} 

}
