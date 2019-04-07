package com.nasnav.persistence;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrganizationRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "organizations")
@Data
@EqualsAndHashCode(callSuper = false)
//@NamedQuery(name = "User.findByTheUsersName", query = "from User u where u.username = ?1")
public class OrganizationEntity extends AbstractPersistable<Long> implements BaseEntity {

    public enum Type { Brand, Mall, Store, Pharmacies, Unknown }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;
    private String type;
    @Column(name = "p_name")
    private String pname;
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt = new Date();

//    @OneToOne(mappedBy = "organizationEntity")
//    @JsonIgnore
//    private OrganizationThemeEntity organizationThemeEntity;

//    @OneToOne(mappedBy = "organizationEntity")
//    @JsonIgnore
//    private SocialEntity socialEntity;
    
//    @OneToMany(mappedBy="organizationEntity")
//    @JsonIgnore
//    private Set<OrdersEntity> ordersEntity;

//    @OneToMany(mappedBy="organizationEntity")
//    @JsonIgnore
//    private Set<StocksEntity> stocksEntities;


    public OrganizationEntity() {
        id = null;
    }

    public Type getType() {
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
        organizationRepresentationObject.setType(getType()!=null?getType().name():null);
        return organizationRepresentationObject;
    }

}
