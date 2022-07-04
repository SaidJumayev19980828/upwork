package com.nasnav.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Builder
public class ImportProcessStatusResponse {

    private HandlerChainProcessStatus processStatus;

    private Long orgId;

    private Long userId;

}
