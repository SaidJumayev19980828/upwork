package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.enumerations.VideoChatOrgState;
import com.nasnav.enumerations.YeshteryState;
import com.nasnav.dto.OrganizationRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "organizations")
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationEntity implements BaseEntity {

    public enum Type { Brand, Mall, Store, Pharmacies, Unknown }

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;
    private String type;
    
    @Column(name = "p_name")
    private String pname;

    @Column(name = "theme_id")
    private Integer themeId;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "organization_theme_classes"
            ,joinColumns = {@JoinColumn(name="organization_id")}
            ,inverseJoinColumns = {@JoinColumn(name="theme_class_id")})
    private Set<ThemeClassEntity> themeClasses;

    @Column(name = "extra_info")
    private String extraInfo;

    @Column(name = "ecommerce")
    private Integer ecommerce;

    @Column(name = "google_token")
    private String googleToken;

    @Column(name = "facebook_token")
    private String facebookToken;

    @Column(name = "matomo")
    private Integer matomoId;

    @Column(name = "facebook_pixel")
    private String pixelId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_iso", referencedColumnName = "iso_code")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CountriesEntity country;

    @Column(name = "yeshtery_state")
    private Integer yeshteryState;

    @Column(name = "priority")
    private Integer priority;

    @OneToMany(mappedBy = "organizationEntity")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<ShopsEntity> shops;

    @OneToMany(mappedBy = "organizationEntity")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<OrganizationImagesEntity> images;

    @Column(name = "enable_video_chat")
    private Integer enableVideoChat;

    @OneToOne
    @JoinColumn(name = "notification_topic")
    private NotificationTopicsEntity notificationTopic;


    public OrganizationEntity() {
        id = null;
        this.ecommerce = 1;
        this.yeshteryState = 0;
        this.enableVideoChat = 0;
    }

    public Type getType() {
    	if(this.type == null)
    		return Type.Unknown;
    	
        switch (this.type) {
            case "BrandRepresentationObject":
                return Type.Brand;
            case "Mall":
                return Type.Mall;
            case "Store":
                return Type.Store;
            case "Pharmacies":
                return Type.Pharmacies;
            default:
                return Type.Unknown;
        }
    }
    
    

    @Override
    public String toString() {
        return String.format(
                "OrganizationEntity[id=%d, name='%s', p_name='%s', type='%s']",
                id, name, pname, type);
    }


    @Override
    public BaseRepresentationObject getRepresentation() {
        var obj = new OrganizationRepresentationObject();
        obj.setId(getId());
        obj.setDescription(getDescription());
        obj.setName(getName());
        obj.setPname(getPname());
        obj.setType(getType()!=null?getType().name():null);
        obj.setThemeId(getThemeId().toString());
        obj.setEcommerce((getEcommerce()));
        obj.setGoogleToken(getGoogleToken());
        obj.setFacebookToken(getFacebookToken());
        obj.setMatomoSiteId(getMatomoId());
        obj.setPixelSiteId(getPixelId());
        obj.setPriority(getPriority());
        if (yeshteryState != null) {
            obj.setYeshteryState(yeshteryState == 1);
        }

        obj.setEnableVideoChat(enableVideoChat.equals(1));

        if(getCountry() != null) {
            obj.setCurrency(getCountry().getCurrency());
            obj.setCurrencyIso(getCountry().getIsoCode());
        }
        if (getExtraInfo() != null) {
            try {
                JSONObject json = new JSONObject(getExtraInfo());
                obj.setInfo(json.toMap());
            } catch (Exception e) {}
        }
        return obj;
    }

}
