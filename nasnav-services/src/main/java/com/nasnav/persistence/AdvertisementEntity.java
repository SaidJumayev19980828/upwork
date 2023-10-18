package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "advertisements")
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
public class AdvertisementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private OrganizationEntity organization;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "from_date")
    private LocalDateTime fromDate;

    @Column(name = "to_date")
    private LocalDateTime toDate;

    @Column(name = "created_at")
    @CreationTimestamp
    @CreatedDate
    private LocalDateTime creationDate;

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PostEntity> posts = new ArrayList<>();

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AdvertisementProductEntity> advertisementProducts = new ArrayList<>();
}
