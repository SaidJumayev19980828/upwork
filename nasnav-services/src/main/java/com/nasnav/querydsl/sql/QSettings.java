package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSettings is a Querydsl query type for QSettings
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSettings extends com.querydsl.sql.RelationalPathBase<QSettings> {

    private static final long serialVersionUID = -1197394983;

    public static final QSettings settings = new QSettings("settings");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath settingName = createString("settingName");

    public final StringPath settingValue = createString("settingValue");

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QSettings> settingsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> settingsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QSettings(String variable) {
        super(QSettings.class, forVariable(variable), "public", "settings");
        addMetadata();
    }

    public QSettings(String variable, String schema, String table) {
        super(QSettings.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSettings(String variable, String schema) {
        super(QSettings.class, forVariable(variable), schema, "settings");
        addMetadata();
    }

    public QSettings(Path<? extends QSettings> path) {
        super(path.getType(), path.getMetadata(), "public", "settings");
        addMetadata();
    }

    public QSettings(PathMetadata metadata) {
        super(QSettings.class, metadata, "public", "settings");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(4).ofType(Types.BIGINT).withSize(19));
        addMetadata(settingName, ColumnMetadata.named("setting_name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(settingValue, ColumnMetadata.named("setting_value").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(type, ColumnMetadata.named("type").withIndex(5).ofType(Types.INTEGER).withSize(10));
    }

}

