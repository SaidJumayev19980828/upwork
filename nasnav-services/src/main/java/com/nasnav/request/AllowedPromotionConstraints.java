package com.nasnav.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class AllowedPromotionConstraints extends BaseSearchParams{
    private Set<Long> productIds;
    private Set<Long> brandIds;
    private Set<Long> tagIds;

    public AllowedPromotionConstraints() {
        this.productIds = new HashSet<>();
        this.brandIds = new HashSet<>();
        this.tagIds = new HashSet<>();
    }

    public void addBrandIds(Set<Long> brandIds) {
        this.brandIds.addAll(brandIds);
    }

    public void addProductIds(Set<Long> productIds) {
        this.productIds.addAll(productIds);
    }

    public void addTagIds(Set<Long> tagIds) {
        this.tagIds.addAll(tagIds);
    }
}
