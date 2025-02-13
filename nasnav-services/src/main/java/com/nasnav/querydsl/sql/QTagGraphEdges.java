package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTagGraphEdges is a Querydsl query type for QTagGraphEdges
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTagGraphEdges extends com.querydsl.sql.RelationalPathBase<QTagGraphEdges> {

    private static final long serialVersionUID = 166692588;

    public static final QTagGraphEdges tagGraphEdges = new QTagGraphEdges("tag_graph_edges");

    public final NumberPath<Long> childId = createNumber("childId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> parentId = createNumber("parentId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QTagGraphEdges> tagGraphEdgesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QTagGraphNodes> tagEdgesChildIdFkey = createForeignKey(childId, "id");

    public final com.querydsl.sql.ForeignKey<QTagGraphNodes> tagEdgesParentIdFkey = createForeignKey(parentId, "id");

    public QTagGraphEdges(String variable) {
        super(QTagGraphEdges.class, forVariable(variable), "public", "tag_graph_edges");
        addMetadata();
    }

    public QTagGraphEdges(String variable, String schema, String table) {
        super(QTagGraphEdges.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTagGraphEdges(String variable, String schema) {
        super(QTagGraphEdges.class, forVariable(variable), schema, "tag_graph_edges");
        addMetadata();
    }

    public QTagGraphEdges(Path<? extends QTagGraphEdges> path) {
        super(path.getType(), path.getMetadata(), "public", "tag_graph_edges");
        addMetadata();
    }

    public QTagGraphEdges(PathMetadata metadata) {
        super(QTagGraphEdges.class, metadata, "public", "tag_graph_edges");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(childId, ColumnMetadata.named("child_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(parentId, ColumnMetadata.named("parent_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

