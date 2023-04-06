package com.nasnav.enumerations.converters;

import org.springframework.core.convert.converter.Converter;

import com.nasnav.enumerations.ConvertedImageTypes;

/**
 * this converter is for backword compatability with lower case file types in controllers
 */
public class LowerCaseToConvertedImageTypesConverter implements Converter<String, ConvertedImageTypes> {

  @Override
  public ConvertedImageTypes convert(String source) {
    return ConvertedImageTypes.valueOf(source.toUpperCase());
  }
  
}
