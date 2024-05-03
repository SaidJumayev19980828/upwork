package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "received_award")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedAwardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "award_date", nullable = false)
    private LocalDate awardDate;

    @ManyToOne
    @JoinColumn(name = "sub_post_id")
    private SubPostEntity subPost;

    @ManyToOne
    @JoinColumn(name = "compensation_tier", nullable = false)
    private CompensationRuleTierEntity compensationTier;

    @Column(name = "award_description", nullable = false)
    private String awardDescription;

    @Column(name = "award_amount")
    private BigDecimal awardAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    @JsonIgnore
    private OrganizationEntity organization;
}

