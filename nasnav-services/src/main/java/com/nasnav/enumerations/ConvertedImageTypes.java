package com.nasnav.enumerations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ConvertedImageTypes {
  JPG("jpg"), JPEG("jpeg"), PNG("png"), WEBP("webp");

  @Getter
  private final String value;

  @Override
  public String toString() {
    return value;
  }
}
