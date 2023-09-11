package com.nasnav.persistence;

import lombok.*;

import javax.persistence.*;

@Table(name = "user_followers")
@Entity
@Data
public class FollowerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity follower;

    public FollowerEntity(UserEntity userEntity, UserEntity follower) {
        this.setUser(userEntity);
        this.setFollower(follower);
    }

    public FollowerEntity() {

    }
}
