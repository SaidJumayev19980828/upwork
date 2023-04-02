package com.nasnav.dto.response;

import java.util.List;

import com.nasnav.dto.ShopFloorDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FloorsData {
  List<ShopFloorDTO> floors;
}
