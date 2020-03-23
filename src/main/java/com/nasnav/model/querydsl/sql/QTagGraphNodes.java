package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTagGraphNodes is a Querydsl query type for QTagGraphNodes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTagGraphNodes extends com.querydsl.sql.RelationalPathBase<QTagGraphNodes> {

    private static final long serialVersionUID = 1673630850;

    public static final QTagGraphNodes tagGraphNodes = new QTagGraphNodes("tag_graph_nodes");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> tagId = createNumber("tagId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QTagGraphNodes> tagGraphNodesPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QTags> tagGraphNodesFk = createForeignKey(tagId, "id");

    public final com.querydsl.sql.ForeignKey<QTagGraphEdges> _tagGraphEdgesChildFk = createInvForeignKey(id, "child_id");

    public final com.querydsl.sql.ForeignKey<QTagGraphEdges> _tagGraphEdgesParentFk = createInvForeignKey(id, "parent_id");

    public QTagGraphNodes(String variable) {
        super(QTagGraphNodes.class, forVariable(variable), "public", "tag_graph_nodes");
        addMetadata();
    }

    public QTagGraphNodes(String variable, String schema, String table) {
        super(QTagGraphNodes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTagGraphNodes(String variable, String schema) {
        super(QTagGraphNodes.class, forVariable(variable), schema, "tag_graph_nodes");
        addMetadata();
    }

    public QTagGraphNodes(Path<? extends QTagGraphNodes> path) {
        super(path.getType(), path.getMetadata(), "public", "tag_graph_nodes");
        addMetadata();
    }

    public QTagGraphNodes(PathMetadata metadata) {
        super(QTagGraphNodes.class, metadata, "public", "tag_graph_nodes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(tagId, ColumnMetadata.named("tag_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

