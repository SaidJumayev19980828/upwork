package com.nasnav.dto;

import lombok.Data;

import java.util.List;
@Data
public class AppliedTo {
    private boolean required;
    private List<Long> ids;
}
