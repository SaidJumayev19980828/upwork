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

@Table(name = "tag_graph_nodes")
@Entity
@Data
@NoArgsConstructor
public class TagGraphNodeEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	
	@ManyToOne
	@JoinColumn(name = "tag_id", nullable = false)
	private TagsEntity tag;
	
	
	
	public TagGraphNodeEntity(TagsEntity tag) {
		this.tag = tag;
	}
	
}
