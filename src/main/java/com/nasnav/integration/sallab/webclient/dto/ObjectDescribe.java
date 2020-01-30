package com.nasnav.integration.sallab.webclient.dto;

import lombok.Data;

import java.util.List;

@Data
public class ObjectDescribe {

    private boolean activateable;
    private boolean createable;
    private boolean custom;
    private boolean customSetting;
    private boolean deletable;
    private boolean deprecatedAndHidden;
    private boolean feedEnabled;
    private boolean hasSubtypes;
    private boolean isSubtype;
    private String keyPrefix;
    private String label;
    private String labelPlural;
    private boolean layoutable;
    private boolean mergeable;
    private boolean mruEnabled;
    private String name;
    private boolean queryable;
    private boolean replicateable;
    private boolean retrieveable;
    private boolean searchable;
    private boolean triggerable;
    private boolean undeletable;
    private boolean updateable;
    private Urls urls;
}
