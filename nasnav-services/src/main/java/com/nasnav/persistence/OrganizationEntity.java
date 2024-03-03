package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.enumerations.DiscountStrategies;
import com.nasnav.enumerations.DiscountStrategiesConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.json.JSONObject;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Column(name = "google_analytics_site_id")
    private String googleAnalyticsSiteId;

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

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "opening_hours")
    private String openingHours;


    @OneToMany(mappedBy = "organizationEntity",fetch = FetchType.EAGER)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<ShopsEntity> shops;

    @OneToMany(mappedBy = "organizationEntity" ,fetch = FetchType.EAGER)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<OrganizationImagesEntity> images;

    @Column(name = "enable_video_chat")
    private Integer enableVideoChat;

    @OneToOne(mappedBy = "organization", fetch = FetchType.LAZY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private PackageRegisteredEntity packageRegistration;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EmployeeUserEntity owner;


    @OneToOne(mappedBy = "organization", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    @JsonIgnore
    private BankAccountEntity bankAccount;
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY,cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Set<SubscriptionEntity> subscriptions;


    @Convert(converter = DiscountStrategiesConverter.class)
    @Column(name = "strategies")
    private Set<DiscountStrategies> discountStrategies;
    public OrganizationEntity() {
        id = null;
        this.ecommerce = 1;
        this.yeshteryState = 0;
        this.enableVideoChat = 0;
    }

    public  <E extends Enum<E>> Map<E, Boolean> getDiscountStrategies (Set<E> enumValues, Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(
                        enumValue -> enumValue,
                        enumValues::contains
                ));
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
        obj.setShortDescription(getShortDescription());
        obj.setOpeningHours(getOpeningHours());
        obj.setPixelSiteId(getPixelId());
        obj.setPriority(getPriority());
        obj.setGoogleAnalyticsSiteId(getGoogleAnalyticsSiteId());
        obj.setStrategies(getDiscountStrategies(discountStrategies, DiscountStrategies.class));
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
        if(this.bankAccount != null)
            obj.setBankAccountId(this.bankAccount.getId());

        return obj;
    }

}
