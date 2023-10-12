package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(name = "events")
@Entity
@Data
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(name = "event_influencers"
            ,joinColumns = {@JoinColumn(name="event_id")}
            ,inverseJoinColumns = {@JoinColumn(name="influencer_id")})
    private Set<InfluencerEntity> influencers;


    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<EventAttachmentsEntity> attachments;

    @Column(name = "visible")
    private Boolean visible;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private Integer status;

    @Column(name = "coin")
    private Long coin;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "event_products"
            ,joinColumns = {@JoinColumn(name="event_id")}
            ,inverseJoinColumns = {@JoinColumn(name="product_id")})
    private List<ProductEntity> products;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private EventRoomTemplateEntity roomTemplate;


    public void addInfluencer(InfluencerEntity influencer) {
        if (influencer != null) {
            if (getInfluencers() == null)
                setInfluencers(new HashSet<>());
            if (!getInfluencers().contains(influencer))
                getInfluencers().add(influencer);
             }
        }



    }





