package com.nasnav.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
	
	private Integer priority;

	public TagGraphNodeEntity(TagsEntity tag, Integer priority) {
		this.tag = tag;
		this.priority = priority;
	}
}
