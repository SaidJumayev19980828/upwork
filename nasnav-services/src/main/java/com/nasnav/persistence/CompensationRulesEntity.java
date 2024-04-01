package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "compensation_rules")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CompensationRulesEntity {
    @Id
    @GeneratedValue(strategy=IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "action_id")
    private CompensationActionsEntity action;


    @ManyToOne
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @Column(name = "is_active", columnDefinition = "boolean default true", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    Set<CompensationRuleTierEntity> tiers = new HashSet<>();


    public void addTier(CompensationRuleTierEntity tier) {
        if (tier != null){
            if (isDuplicateCondition(tier)) return;
            tier.setRule(this);
            tiers.add(tier);
        }
    }

    private boolean isDuplicateCondition(CompensationRuleTierEntity tier) {
        return tiers.stream().anyMatch(t ->
                Objects.equals(tier.getCondition(), t.getCondition())
                && Objects.equals(tier.isActive(), t.isActive())
        );
    }

}
