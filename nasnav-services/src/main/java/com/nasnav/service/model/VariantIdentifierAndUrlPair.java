package com.nasnav.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class VariantIdentifierAndUrlPair{
	private String url;
	private List<VariantIdentifier> identifier;
}