package com.nasnav.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class AppliedTo {
    private boolean required;
    private Set<Long> ids;
}
