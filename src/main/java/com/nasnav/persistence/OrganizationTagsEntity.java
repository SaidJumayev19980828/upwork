package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrganizationTagsRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import org.json.JSONObject;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Table(name = "organization_tags")
@Entity
@Data
@DiscriminatorValue("1")
@SQLDelete(sql = "UPDATE organization_tags SET removed = 1 WHERE id = ?")
@Loader(namedQuery = "findTagById")
@NamedQuery(name = "findTagById", query = "SELECT p FROM OrganizationTagsEntity p WHERE p.id=?1 AND p.removed = 0")
@Where(clause = "removed = 0")
@EqualsAndHashCode(callSuper=false)
public class OrganizationTagsEntity extends AbstractPersistable<Long> implements BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alias")
    private String alias;

    @Column(name = "p_name")
    private String pname;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "removed")
    private int removed;

    @OneToOne
    @JoinColumn(name = "tag_id", referencedColumnName = "id")
    @JsonIgnore
    private TagsEntity tagsEntity;

    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<ProductEntity> products;

    @Override
    public BaseRepresentationObject getRepresentation() {
        OrganizationTagsRepresentationObject obj = new OrganizationTagsRepresentationObject();
        obj.setId(getId());
        obj.setAlias(getAlias());
        obj.setPname(getPname());
        obj.setMetadata(getMetadata());

        return obj;
    }

}
