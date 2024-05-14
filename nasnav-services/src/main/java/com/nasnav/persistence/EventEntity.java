package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Table(name = "events")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
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


    @Column(name = "access_code", unique = true)
    @Generated
    @GenericGenerator(name = "random-string", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "random-string")
    private String accessCode;


    public void addInfluencer(InfluencerEntity influence) {
        if (influence != null) {
            if (getInfluencers() == null)
                setInfluencers(new HashSet<>());
            if (!influencedExists(influence))
                getInfluencers().add(influence);
             }
        }

        private boolean influencedExists(InfluencerEntity influence) {
            return  influencers.stream().anyMatch(inf ->
                    Objects.equals(influence.getId(), inf.getId())
            );
    }



    private String generateAccessCode() {
        int length = 6; // for example, a 6-character code
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            char randomChar = characters.charAt(random.nextInt(characters.length()));
            code.append(randomChar);
        }
        return code.toString();
    }

    @PrePersist
    private void ensureAccessCode() {
        if (this.accessCode == null || this.accessCode.isEmpty()) {
            this.accessCode = generateAccessCode();
        }
    }
}





