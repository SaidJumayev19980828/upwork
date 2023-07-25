package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "advertisements")
@Entity
@Getter
@Setter
public class AdvertisementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id",nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ProductEntity product;

    @Column(name = "coins")
    private Integer coins;

    @Column(name = "likes")
    private Integer likes;

    @Column(name = "max_coins")
    private Integer maxCoins;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime creationDate;

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.REMOVE)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PostEntity> posts = new ArrayList<>();
}
