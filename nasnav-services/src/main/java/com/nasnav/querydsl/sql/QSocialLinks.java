package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSocialLinks is a Querydsl query type for QSocialLinks
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSocialLinks extends com.querydsl.sql.RelationalPathBase<QSocialLinks> {

    private static final long serialVersionUID = 517720278;

    public static final QSocialLinks socialLinks = new QSocialLinks("social_links");

    public final StringPath facebook = createString("facebook");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath instagram = createString("instagram");

    public final StringPath linkedin = createString("linkedin");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath pinterest = createString("pinterest");

    public final StringPath twitter = createString("twitter");

    public final StringPath youtube = createString("youtube");

    public final com.querydsl.sql.PrimaryKey<QSocialLinks> socialLinksPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> rails9b1a7e5d8eFk = createForeignKey(organizationId, "id");

    public QSocialLinks(String variable) {
        super(QSocialLinks.class, forVariable(variable), "public", "social_links");
        addMetadata();
    }

    public QSocialLinks(String variable, String schema, String table) {
        super(QSocialLinks.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSocialLinks(String variable, String schema) {
        super(QSocialLinks.class, forVariable(variable), schema, "social_links");
        addMetadata();
    }

    public QSocialLinks(Path<? extends QSocialLinks> path) {
        super(path.getType(), path.getMetadata(), "public", "social_links");
        addMetadata();
    }

    public QSocialLinks(PathMetadata metadata) {
        super(QSocialLinks.class, metadata, "public", "social_links");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(facebook, ColumnMetadata.named("facebook").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(instagram, ColumnMetadata.named("instagram").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(linkedin, ColumnMetadata.named("linkedin").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
        addMetadata(pinterest, ColumnMetadata.named("pinterest").withIndex(8).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(twitter, ColumnMetadata.named("twitter").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(youtube, ColumnMetadata.named("youtube").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

