package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "paymob_source")
public class PaymobSourceEntity {

    @Id
    @GeneratedValue(strategy=IDENTITY)
    private Long id;

    private String value;
    private String name;

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private OrganizationEntity organization;

    private String type;

    private String status;

    private String currency;

    private String script;

    private String icon;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String identifier;
}
