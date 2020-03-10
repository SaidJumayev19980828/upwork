package com.nasnav.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;


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
