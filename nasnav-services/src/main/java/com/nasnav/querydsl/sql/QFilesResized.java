package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFilesResized is a Querydsl query type for QFilesResized
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFilesResized extends com.querydsl.sql.RelationalPathBase<QFilesResized> {

    private static final long serialVersionUID = 979771503;

    public static final QFilesResized filesResized = new QFilesResized("files_resized");

    public final NumberPath<Integer> height = createNumber("height", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Long> originalFileId = createNumber("originalFileId", Long.class);

    public final NumberPath<Integer> width = createNumber("width", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QFilesResized> filesResizedPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFiles> filesResizedOriginalFileIdFkey = createForeignKey(originalFileId, "id");

    public QFilesResized(String variable) {
        super(QFilesResized.class, forVariable(variable), "public", "files_resized");
        addMetadata();
    }

    public QFilesResized(String variable, String schema, String table) {
        super(QFilesResized.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFilesResized(String variable, String schema) {
        super(QFilesResized.class, forVariable(variable), schema, "files_resized");
        addMetadata();
    }

    public QFilesResized(Path<? extends QFilesResized> path) {
        super(path.getType(), path.getMetadata(), "public", "files_resized");
        addMetadata();
    }

    public QFilesResized(PathMetadata metadata) {
        super(QFilesResized.class, metadata, "public", "files_resized");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(height, ColumnMetadata.named("height").withIndex(3).ofType(Types.INTEGER).withSize(10));
        addMetadata(id, ColumnMetadata.named("id").withIndex(5).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(imageUrl, ColumnMetadata.named("image_url").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(originalFileId, ColumnMetadata.named("original_file_id").withIndex(1).ofType(Types.BIGINT).withSize(19));
        addMetadata(width, ColumnMetadata.named("width").withIndex(2).ofType(Types.INTEGER).withSize(10));
    }

}

