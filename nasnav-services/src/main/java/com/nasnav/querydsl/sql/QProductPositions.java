package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QProductPositions is a Querydsl query type for QProductPositions
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductPositions extends com.querydsl.sql.RelationalPathBase<QProductPositions> {

    private static final long serialVersionUID = -1379677482;

    public static final QProductPositions productPositions = new QProductPositions("product_positions");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath positionsJsonData = createString("positionsJsonData");

    public final StringPath previewJsonData = createString("previewJsonData");

    public final NumberPath<Long> shop360Id = createNumber("shop360Id", Long.class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QProductPositions> productPositionsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QShop360s> rails7a3b031e76Fk = createForeignKey(shop360Id, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> railsFefa61a65aFk = createForeignKey(organizationId, "id");

    public QProductPositions(String variable) {
        super(QProductPositions.class, forVariable(variable), "public", "product_positions");
        addMetadata();
    }

    public QProductPositions(String variable, String schema, String table) {
        super(QProductPositions.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProductPositions(String variable, String schema) {
        super(QProductPositions.class, forVariable(variable), schema, "product_positions");
        addMetadata();
    }

    public QProductPositions(Path<? extends QProductPositions> path) {
        super(path.getType(), path.getMetadata(), "public", "product_positions");
        addMetadata();
    }

    public QProductPositions(PathMetadata metadata) {
        super(QProductPositions.class, metadata, "public", "product_positions");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
        addMetadata(positionsJsonData, ColumnMetadata.named("positions_json_data").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(previewJsonData, ColumnMetadata.named("preview_json_data").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(shop360Id, ColumnMetadata.named("shop360_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

