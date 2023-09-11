package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Dates {
    public String minCreatedDate;
    public String maxCreatedDate;

    public Dates(LocalDateTime minCreatedDate, LocalDateTime maxCreatedDate) {
        this.minCreatedDate = minCreatedDate.toLocalDate().toString();
        this.maxCreatedDate = maxCreatedDate.toLocalDate().toString();
    }
}
