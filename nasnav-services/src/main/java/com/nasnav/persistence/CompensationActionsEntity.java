package com.nasnav.persistence;

import com.nasnav.enumerations.CompensationActions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "compensation_action")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CompensationActionsEntity {
    @Id
    @GeneratedValue(strategy=IDENTITY)
    private Long id;
    @Column(name = "name",unique = true)
    @Enumerated(EnumType.STRING)
    private CompensationActions name;
    @Column(name = "description")
    private String description;

}
