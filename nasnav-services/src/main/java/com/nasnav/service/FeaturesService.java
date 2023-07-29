package com.nasnav.service;

import java.util.List;
import java.util.Optional;

import com.nasnav.dto.ExtraAttributeDTO;
import com.nasnav.dto.ExtraAttributeDefinitionDTO;
import com.nasnav.dto.ExtraAttributesRepresentationObject;
import com.nasnav.dto.ProductFeatureDTO;
import com.nasnav.dto.ProductFeatureUpdateDTO;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.response.ProductFeatureUpdateResponse;

public interface FeaturesService {

	ProductFeatureUpdateResponse updateProductFeature(ProductFeatureUpdateDTO featureDto);

	List<ProductFeatureDTO> getProductFeatures(Long orgId);

	void removeProductFeature(Integer featureId);

	Optional<Integer> getAdditionalDataExtraAttrId(ProductFeaturesEntity feature);

	List<ExtraAttributesRepresentationObject> getOrganizationExtraAttributesById(Long organizationId);

	Integer createUpdateExtraAttributes(ExtraAttributeDTO extraAttrDTO, String operation);

	void deleteExtraAttribute(Integer attrId);

	List<ExtraAttributeDefinitionDTO> getExtraAttributes();

	String getAdditionalDataExtraAttrName(ProductFeaturesEntity feature);

	public List<ProductFeatureType> getProductFeatureTypes();
}
