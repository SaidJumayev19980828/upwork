package com.nasnav.persistence;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Table(name = "tag_graph_edges")
@Entity
@Data
@NoArgsConstructor
public class TagGraphEdgesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="child_id")
    private TagGraphNodeEntity child;

    @ManyToOne
    @JoinColumn(name="parent_id")
    private TagGraphNodeEntity parent;
    
    
    public TagGraphEdgesEntity(TagGraphNodeEntity parent, TagGraphNodeEntity child) {
    	this.parent = parent;
    	this.child = child;
    }

}
