package com.nasnav.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class DomainOrgIdResponse {
  Long id;

  Long subDir;
}
