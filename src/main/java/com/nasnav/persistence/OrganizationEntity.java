package com.nasnav.persistence;

import javax.persistence.*;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "organizations")
//@NamedQuery(name = "User.findByTheUsersName", query = "from User u where u.username = ?1")
public class OrganizationEntity extends AbstractPersistable<Long> {

    public enum Type { Brand, Mall, Store, Pharmacies, Unknown }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;
    private String type;
    private String p_name;

    public OrganizationEntity() {
        id = null;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        switch (this.type) {
            case "Brand":
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

    public void setType(Type type) {
        this.type = type.name();
    }

    public String getPName() {
        return p_name;
    }

    public void setPName(String p_name) {
        this.p_name = p_name;
    }

    @Override
    public String toString() {
        return String.format(
                "OrganizationEntity[id=%d, name='%s', p_name='%s', type='%s']",
                id, name, p_name, type);
    }


}
