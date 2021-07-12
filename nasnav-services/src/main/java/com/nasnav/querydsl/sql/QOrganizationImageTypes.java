package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganizationImageTypes is a Querydsl query type for QOrganizationImageTypes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizationImageTypes extends com.querydsl.sql.RelationalPathBase<QOrganizationImageTypes> {

    private static final long serialVersionUID = 998917799;

    public static final QOrganizationImageTypes organizationImageTypes = new QOrganizationImageTypes("organization_image_types");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QOrganizationImageTypes> organizationImageTypesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizationImages> _organizationImagesTypeFk = createInvForeignKey(id, "type");

    public QOrganizationImageTypes(String variable) {
        super(QOrganizationImageTypes.class, forVariable(variable), "public", "organization_image_types");
        addMetadata();
    }

    public QOrganizationImageTypes(String variable, String schema, String table) {
        super(QOrganizationImageTypes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizationImageTypes(String variable, String schema) {
        super(QOrganizationImageTypes.class, forVariable(variable), schema, "organization_image_types");
        addMetadata();
    }

    public QOrganizationImageTypes(Path<? extends QOrganizationImageTypes> path) {
        super(path.getType(), path.getMetadata(), "public", "organization_image_types");
        addMetadata();
    }

    public QOrganizationImageTypes(PathMetadata metadata) {
        super(QOrganizationImageTypes.class, metadata, "public", "organization_image_types");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

