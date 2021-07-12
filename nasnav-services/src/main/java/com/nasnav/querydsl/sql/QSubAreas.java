package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSubAreas is a Querydsl query type for QSubAreas
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSubAreas extends com.querydsl.sql.RelationalPathBase<QSubAreas> {

    private static final long serialVersionUID = -444402980;

    public static final QSubAreas subAreas = new QSubAreas("sub_areas");

    public final NumberPath<Long> areaId = createNumber("areaId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigInteger> latitude = createNumber("latitude", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> longitude = createNumber("longitude", java.math.BigInteger.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QSubAreas> subAreasPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QAreas> subAreasAreaIdFkey = createForeignKey(areaId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizations> subAreasOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QAddresses> _addressesSubAreaIdFkey = createInvForeignKey(id, "sub_area_id");

    public QSubAreas(String variable) {
        super(QSubAreas.class, forVariable(variable), "public", "sub_areas");
        addMetadata();
    }

    public QSubAreas(String variable, String schema, String table) {
        super(QSubAreas.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSubAreas(String variable, String schema) {
        super(QSubAreas.class, forVariable(variable), schema, "sub_areas");
        addMetadata();
    }

    public QSubAreas(Path<? extends QSubAreas> path) {
        super(path.getType(), path.getMetadata(), "public", "sub_areas");
        addMetadata();
    }

    public QSubAreas(PathMetadata metadata) {
        super(QSubAreas.class, metadata, "public", "sub_areas");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(areaId, ColumnMetadata.named("area_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(latitude, ColumnMetadata.named("latitude").withIndex(6).ofType(Types.NUMERIC).withSize(131089));
        addMetadata(longitude, ColumnMetadata.named("longitude").withIndex(5).ofType(Types.NUMERIC).withSize(131089));
        addMetadata(name, ColumnMetadata.named("name").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

