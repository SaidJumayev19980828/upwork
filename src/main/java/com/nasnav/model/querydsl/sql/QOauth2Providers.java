package com.nasnav.model.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOauth2Providers is a Querydsl query type for QOauth2Providers
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOauth2Providers extends com.querydsl.sql.RelationalPathBase<QOauth2Providers> {

    private static final long serialVersionUID = -679321492;

    public static final QOauth2Providers oauth2Providers = new QOauth2Providers("oauth2_providers");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath providerName = createString("providerName");

    public final com.querydsl.sql.PrimaryKey<QOauth2Providers> oauth2ProvidersPk = createPrimaryKey(id);

    public QOauth2Providers(String variable) {
        super(QOauth2Providers.class, forVariable(variable), "public", "oauth2_providers");
        addMetadata();
    }

    public QOauth2Providers(String variable, String schema, String table) {
        super(QOauth2Providers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOauth2Providers(String variable, String schema) {
        super(QOauth2Providers.class, forVariable(variable), schema, "oauth2_providers");
        addMetadata();
    }

    public QOauth2Providers(Path<? extends QOauth2Providers> path) {
        super(path.getType(), path.getMetadata(), "public", "oauth2_providers");
        addMetadata();
    }

    public QOauth2Providers(PathMetadata metadata) {
        super(QOauth2Providers.class, metadata, "public", "oauth2_providers");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(providerName, ColumnMetadata.named("provider_name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

