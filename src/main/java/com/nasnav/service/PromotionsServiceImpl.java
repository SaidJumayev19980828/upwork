package com.nasnav.service;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.dto.PromotionSearchParamDTO;
import com.nasnav.dto.response.PromotionDTO;




@Service
public class PromotionsServiceImpl implements PromotionsService {

	@Autowired
	private EntityManager entityMgr;
	
	
	
	@Override
	public List<PromotionDTO> getPromotions(PromotionSearchParamDTO searchParams) {
		
		
		
		return null;
	}

}
