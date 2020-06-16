package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.CategoryRepresentationObject;

import lombok.Data;

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




    //@Override
    public BaseRepresentationObject getRepresentation() {
        CategoryRepresentationObject categoryRepresentationObject = new CategoryRepresentationObject();
        categoryRepresentationObject.setId(getId());
        categoryRepresentationObject.setName(getName());
        categoryRepresentationObject.setPname(getPname());
        categoryRepresentationObject.setLogo(getLogo());
        if (getParentId() != null) {
            categoryRepresentationObject.setParentId(getParentId());
        }
        return categoryRepresentationObject;
    }
}
