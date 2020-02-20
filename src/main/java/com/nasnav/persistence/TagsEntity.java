package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.TagsRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;

import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Set;

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
        TagsRepresentationObject obj = new TagsRepresentationObject();
        obj.setId(getId());
        obj.setName(getName());
        obj.setAlias(getAlias());
        obj.setPname(getPname());
        obj.setMetadata(getMetadata());
        obj.setCategoryId(categoriesEntity.getId());
        obj.setGraphId(getGraphId());
        
        return obj;
    }

}
