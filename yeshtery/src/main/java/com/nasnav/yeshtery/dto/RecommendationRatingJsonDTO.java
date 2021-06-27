package com.nasnav.yeshtery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nasnav.dto.BaseJsonDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Schema(name = "Recommendation Rating data")
@JsonPropertyOrder({"product_id", "tag_id", "product_name", "total_count", "total_rate", "rate"})
@EqualsAndHashCode(callSuper=false)
public class RecommendationRatingJsonDTO extends BaseJsonDTO {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("tag_id")
    private Long tagId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("total_count")
    private Long totalCount;

    @JsonProperty("total_rate")
    private Long totalRate;

    private int rate;

    @Override
    protected void initRequiredProperties() {
    }

    public void setProductId(Long productId) {
        setPropertyAsUpdated("product_id");
        this.productId = productId;
    }

    public void setTagId(Long tagId) {
        setPropertyAsUpdated("tag_id");
        this.tagId = tagId;
    }

    public void setProductName(String productName) {
        setPropertyAsUpdated("product_name");
        this.productName = productName;
    }

    public void setTotalCount(Long totalCount) {
        setPropertyAsUpdated("total_count");
        this.totalCount = totalCount;
    }

    public void setTotalRate(Long totalRate) {
        setPropertyAsUpdated("total_rate");
        this.totalRate = totalRate;
    }

    public void setRate(int rate) {
        setPropertyAsUpdated("rate");
        this.rate = rate;
    }
}
