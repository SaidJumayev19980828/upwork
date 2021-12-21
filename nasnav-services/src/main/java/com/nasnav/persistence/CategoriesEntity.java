package com.nasnav.persistence;

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
    private Long parentId;

    private String cover;

    @Column(name = "cover_small")
    private String coverSmall;

    //@Override
    public BaseRepresentationObject getRepresentation() {
        CategoryRepresentationObject categoryRepresentationObject = new CategoryRepresentationObject();
        categoryRepresentationObject.setId(getId());
        categoryRepresentationObject.setName(getName());
        categoryRepresentationObject.setPname(getPname());
        categoryRepresentationObject.setLogo(getLogo());
        categoryRepresentationObject.setCover(getCover());
        categoryRepresentationObject.setCoverSmall(getCoverSmall());
        if (getParentId() != null) {
            categoryRepresentationObject.setParentId(getParentId());
        }
        return categoryRepresentationObject;
    }
}
