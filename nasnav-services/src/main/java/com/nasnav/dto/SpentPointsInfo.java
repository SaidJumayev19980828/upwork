package com.nasnav.dto;

import com.nasnav.persistence.LoyaltyPointTransactionEntity;
import com.nasnav.persistence.LoyaltySpentTransactionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class SpentPointsInfo {
    private List<LoyaltyPointTransactionEntity> spentPoints;
    private List<LoyaltySpentTransactionEntity> spentPointsRef;

    public SpentPointsInfo() {
        spentPoints = new ArrayList<>();
        spentPointsRef = new ArrayList<>();
    }
}
