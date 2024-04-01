package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Table(name = "compensation_rule_tier")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompensationRuleTierEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "condition", nullable = false)
    private long condition;

    @Column(name = "reward", nullable = false, columnDefinition = "NUMERIC DEFAULT 0.0")
    private BigDecimal reward;

    @Column(name = "is_active", columnDefinition = "boolean default true", nullable = false)
    private boolean isActive;


    @ManyToOne
    @JoinColumn(name = "rule_id")
    @JsonBackReference
    private CompensationRulesEntity rule;

}
