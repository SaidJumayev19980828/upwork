package com.nasnav.dto.request.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.BaseJsonDTO;
import com.nasnav.enumerations.YeshteryState;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrganizationCreationDTO extends BaseJsonDTO {
    private Long id;
    private String name;
    @JsonProperty("p_name")
    private String pname;
    private Integer ecommerce;
    private String googleToken;
    private String facebookToken;
    private Integer currencyIso;
    private Boolean yeshteryState;
    private Integer priority;
    private Boolean enableVideoChat;
    private Integer matomoSiteId;
    private Integer pixelSiteId;

    @Override
    protected void initRequiredProperties() {

    }

    public void setId(Long id) {
        setPropertyAsUpdated("id");
        this.id = id;
    }

    public void setName(String name) {
        setPropertyAsUpdated("name");
        this.name = name;
    }

    public void setPname(String pname) {
        setPropertyAsUpdated("pname");
        this.pname = pname;
    }

    public void setEcommerce(Integer ecommerce) {
        setPropertyAsUpdated("ecommerce");
        this.ecommerce = ecommerce;
    }

    public void setGoogleToken(String googleToken) {
        setPropertyAsUpdated("googleToken");
        this.googleToken = googleToken;
    }

    public void setFacebookToken(String facebookToken) {
        setPropertyAsUpdated("facebookToken");
        this.facebookToken = facebookToken;
    }

    public void setCurrencyIso(Integer currencyIso) {
        setPropertyAsUpdated("currencyIso");
        this.currencyIso = currencyIso;
    }

    public void setYeshteryState(Boolean yeshteryState) {
        setPropertyAsUpdated("yeshteryState");
        this.yeshteryState = yeshteryState;
    }

    public void setPriority(Integer priority) {
        setPropertyAsUpdated("priority");
        this.priority = priority;
    }

    public void setEnableVideoChat(Boolean enableVideoChat) {
        setPropertyAsUpdated("enableVideoChat");
        this.enableVideoChat = enableVideoChat;
    }

    public void setMatomoSiteId(Integer matomoSiteId) {
        setPropertyAsUpdated("matomoSiteId");
        this.matomoSiteId = matomoSiteId;
    }

    public void setPixelSiteId(Integer pixelSiteId) {
        setPropertyAsUpdated("pixelSiteId");
        this.pixelSiteId = pixelSiteId;
    }
}
