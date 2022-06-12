package com.nasnav.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@AllArgsConstructor
public class PromotionsSearchParams extends BaseSearchParams{
    public Optional<Integer> status;
    public Optional<LocalDateTime> startTime;
    public Optional<LocalDateTime> endTime;
    public Optional<Long> id;
    public Integer start;
    public Integer count;
}