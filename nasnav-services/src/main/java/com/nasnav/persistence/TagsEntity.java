package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.TagsRepresentationObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Table(name = "tags")
@Entity
@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class TagsEntity extends AbstractPersistable<Long> implements BaseEntity{

    public TagsEntity() {
        allowReward = false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tags_seq")
    @SequenceGenerator(name = "tags_seq", sequenceName = "tags_id_seq")
    private Long id;


    @Column(name = "name")
    private String name;

    @Column(name = "alias")
    private String alias;

    @Column(name = "p_name")
    private String pname;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "removed")
    private int removed;

    @Column(name = "graph_id")
    private Integer graphId;

    @Column(name = "allow_reward")
    private Boolean allowReward;

    @OneToOne
    @JoinColumn(name = "minimum_tier_id", referencedColumnName = "id")
    @JsonIgnore
    private LoyaltyTierEntity minimumTier;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @JsonIgnore
    private CategoriesEntity categoriesEntity;

    private Integer priority;

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private OrganizationEntity organizationEntity;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private Set<ProductEntity> products;

    @Override
    public BaseRepresentationObject getRepresentation() {
    	Long categoryId = ofNullable(categoriesEntity).map(CategoriesEntity::getId).orElse(null);
        Long orgId = ofNullable(organizationEntity).map(OrganizationEntity::getId).orElse(null);
        TagsRepresentationObject obj = new TagsRepresentationObject();
        obj.setId(getId());
        obj.setName(getName());
        obj.setAlias(getAlias());
        obj.setPname(getPname());
        obj.setMetadata(getMetadata());
        obj.setCategoryId(categoryId);
        obj.setOrgId(orgId);
        obj.setPriority(getPriority());
        obj.setAllowReward(getAllowReward());
        if (getMinimumTier() != null)
            obj.setMinimumTierId(getMinimumTier().getId());
        
        return obj;
    }

}
