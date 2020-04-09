package com.nasnav.persistence;

import static java.util.Optional.ofNullable;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.TagsRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;

@Table(name = "tags")
@Entity
@Data

@EqualsAndHashCode(callSuper=false)
public class TagsEntity extends AbstractPersistable<Long> implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @JsonIgnore
    private CategoriesEntity categoriesEntity;

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    @Exclude
    @lombok.ToString.Exclude
    private Set<ProductEntity> products;

    @Override
    public BaseRepresentationObject getRepresentation() {
    	Long categoryId = ofNullable(categoriesEntity).map(CategoriesEntity::getId).orElse(null);
        TagsRepresentationObject obj = new TagsRepresentationObject();
        obj.setId(getId());
        obj.setName(getName());
        obj.setAlias(getAlias());
        obj.setPname(getPname());
        obj.setMetadata(getMetadata());
        obj.setCategoryId(categoryId);
        obj.setGraphId(getGraphId());
        
        return obj;
    }

}
