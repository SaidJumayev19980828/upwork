package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.CategoryRepresentationObject;
import lombok.Data;

import javax.persistence.*;

@Table(name = "categories")
@Entity
@Data
public class CategoriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "p_name")
    private String pname;
    @Column(name = "logo")
    private String logo;
    @Column(name = "parent_id")
    private Integer parentId;
//    @OneToOne(mappedBy = "categoriesEntity")
//    @JsonIgnore
//    private ProductEntity productEntity;

    //@Override
    public BaseRepresentationObject getRepresentation() {
        CategoryRepresentationObject categoryRepresentationObject = new CategoryRepresentationObject();
        categoryRepresentationObject.setId(getId());
        categoryRepresentationObject.setName(getName());
        categoryRepresentationObject.setPname(getPname());
        categoryRepresentationObject.setLogoUrl(getLogo());
        if (getParentId() != null) {
            categoryRepresentationObject.setParentId(getParentId());
        }
        return categoryRepresentationObject;
    }
}
