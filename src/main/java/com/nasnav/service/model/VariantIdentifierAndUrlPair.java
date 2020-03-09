package com.nasnav.service.model;

import java.util.List;

import com.nasnav.dto.ProductImageUpdateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class VariantIdentifierAndUrlPair{
	private String url;
	private List<ProductImageUpdateIdentifier> identifier;
}