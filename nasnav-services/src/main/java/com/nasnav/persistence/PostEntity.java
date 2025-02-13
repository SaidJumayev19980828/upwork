package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(name = "posts")
@Entity
@Data
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "status")
    private Integer status;

    @Column(name = "type")
    private Integer type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @Column(name = "description")
    private String description;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "post_products"
            ,joinColumns = {@JoinColumn(name="post_id")}
            ,inverseJoinColumns = {@JoinColumn(name="product_id")})
    private List<ProductEntity> products;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PostAttachmentsEntity> attachments = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "advertisement_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private AdvertisementEntity advertisement;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "saved_posts",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> savedByUsers = new HashSet<>();

    @Column(name= "product_name")
    private String productName;

    @Column(name= "ratings")
    private Short rating;

    @ManyToOne
    @JoinColumn(name = "shop")
    private ShopsEntity shop;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL , fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<SubPostEntity> subPosts = new HashSet<>();


    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL , orphanRemoval = true)
    @JsonManagedReference
    private Set<PostLikesEntity> reviewLikes = new HashSet<>();

    public void addAttachment(PostAttachmentsEntity attachment) {
        if (attachment != null) {
            if (!getAttachments().contains(attachment))
                getAttachments().add(attachment);
            attachment.setPost(this);
        }
    }

    public void addSubPost(SubPostEntity subPost) {
        if (subPost != null) {
            subPost.setPost(this);
            getSubPosts().add(subPost);
        }
    }

    public void addLike(PostLikesEntity like) {
        if (like != null) {
            like.setReview(this);
            getReviewLikes().add(like);
        }
    }
}
