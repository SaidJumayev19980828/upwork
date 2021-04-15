package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Urls {
    private String compactLayouts;
    private String rowTemplate;
    private String approvalLayouts;
    private String defaultValues;

    @JsonProperty("listviews")
    private String listViews;

    private String describe;
    private String quickActions;
    private String layouts;
    private String sobject;
}
