package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFiles is a Querydsl query type for QFiles
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFiles extends com.querydsl.sql.RelationalPathBase<QFiles> {

    private static final long serialVersionUID = 135593660;

    public static final QFiles files = new QFiles("files");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    public final StringPath mimetype = createString("mimetype");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath origFilename = createString("origFilename");

    public final StringPath url = createString("url");

    public final com.querydsl.sql.PrimaryKey<QFiles> filesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> filesOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QProductImages> _productImagesFilesUrlKey = createInvForeignKey(url, "uri");

    public final com.querydsl.sql.ForeignKey<QOrganizationImages> _organizationImagesUriFkey = createInvForeignKey(url, "uri");

    public QFiles(String variable) {
        super(QFiles.class, forVariable(variable), "public", "files");
        addMetadata();
    }

    public QFiles(String variable, String schema, String table) {
        super(QFiles.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFiles(String variable, String schema) {
        super(QFiles.class, forVariable(variable), schema, "files");
        addMetadata();
    }

    public QFiles(Path<? extends QFiles> path) {
        super(path.getType(), path.getMetadata(), "public", "files");
        addMetadata();
    }

    public QFiles(PathMetadata metadata) {
        super(QFiles.class, metadata, "public", "files");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(location, ColumnMetadata.named("location").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(mimetype, ColumnMetadata.named("mimetype").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(origFilename, ColumnMetadata.named("orig_filename").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(url, ColumnMetadata.named("url").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

