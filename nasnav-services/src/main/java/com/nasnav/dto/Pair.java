package com.nasnav.dto;

import lombok.Data;
import lombok.Value;

@Data
public class Pair {

    Long first;
    Long second;

    public Pair(Long first, Long second) {
        this.first = first;
        this.second = second;
    }

}
