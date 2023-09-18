package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Table(name = "influencers")
@Entity
@Data
public class InfluencerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "employee_user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EmployeeUserEntity employeeUser;

    @OneToOne
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @Column(name = "approved")
    private Boolean approved;

    @Column(name = "is_guided")
    private Boolean isGuided;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "influencer_categories"
            ,joinColumns = {@JoinColumn(name="influencer_id")}
            ,inverseJoinColumns = {@JoinColumn(name="category_id")})
    private List<CategoriesEntity> categories;

}
