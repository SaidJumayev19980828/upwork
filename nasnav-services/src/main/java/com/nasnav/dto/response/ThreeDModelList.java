package com.nasnav.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ThreeDModelList {
    private Long total;
    private List<ThreeDModelResponse> threeDModels;
}
