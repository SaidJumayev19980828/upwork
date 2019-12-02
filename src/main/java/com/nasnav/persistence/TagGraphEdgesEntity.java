package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.Pair;
import lombok.Data;

import javax.persistence.*;

@SqlResultSetMapping(
        name = "TagsEdgesPair",
        classes = @ConstructorResult(
                targetClass = Pair.class,
                columns = {
                        @ColumnResult(name = "parent_id", type = long.class),
                        @ColumnResult(name = "child_id", type = long.class)
                }))
@NamedNativeQuery(
        name = "TagGraphEdgesEntity.getTagsLinks",
        query = "SELECT t.parent_id, t.child_id FROM tag_graph_edges t WHERE t.child_id in :childIds",
        resultSetMapping = "TagsEdgesPair"
)

@Table(name = "tag_graph_edges")
@Entity
@Data
public class TagGraphEdgesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="child_id")
    private Long childId;

    @Column(name="parent_id")
    private Long parentId;

}
