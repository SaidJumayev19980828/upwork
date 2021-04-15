package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name ="seo_keywords")
@Data
public class SeoKeywordEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "keyword")
    private String keyword;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;
}
