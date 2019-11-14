package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Table(name = "tag_graph_edges")
@Entity
@Data
public class TagGraphEdgesEntity {

    @Column(name="from")
    private Long id;

    @Column(name="to")
    private Long parentId;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    @JsonIgnore
    private OrganizationEntity organizationEntity;
}
