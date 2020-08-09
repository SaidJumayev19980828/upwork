package com.nasnav.service;

import java.util.List;

import com.nasnav.dto.PromotionSearchParamDTO;
import com.nasnav.dto.response.PromotionDTO;

public interface PromotionsService {
	List<PromotionDTO> getPromotions(PromotionSearchParamDTO searchParams);
}
