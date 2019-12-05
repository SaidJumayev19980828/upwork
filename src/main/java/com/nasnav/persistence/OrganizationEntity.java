package com.nasnav.persistence;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "organizations")
@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationEntity implements BaseEntity {

    public enum Type { Brand, Mall, Store, Pharmacies, Unknown }

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;
    private String type;
    
    @Column(name = "p_name")
    private String pname;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "theme_id")
    private Integer themeId;

    public OrganizationEntity() {
        id = null;
    }

    public Type getType() {
    	if(this.type == null)
    		return Type.Unknown;
    	
        switch (this.type) {
            case "BrandRepresentationObject":
                return Type.Brand;
            case "Mall":
                return Type.Mall;
            case "Store":
                return Type.Store;
            case "Pharmacies":
                return Type.Pharmacies;
            default:
                return Type.Unknown;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "OrganizationEntity[id=%d, name='%s', p_name='%s', type='%s']",
                id, name, pname, type);
    }


    @Override
    public BaseRepresentationObject getRepresentation() {
        OrganizationRepresentationObject organizationRepresentationObject = new OrganizationRepresentationObject();
        organizationRepresentationObject.setId(getId());
        organizationRepresentationObject.setDescription(getDescription());
        organizationRepresentationObject.setName(getName());
        organizationRepresentationObject.setPname(getPname());
        organizationRepresentationObject.setType(getType()!=null?getType().name():null);
        organizationRepresentationObject.setThemeId(getThemeId());

        return organizationRepresentationObject;
    }

}
