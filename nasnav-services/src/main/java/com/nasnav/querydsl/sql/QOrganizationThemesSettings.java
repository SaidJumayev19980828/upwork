package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganizationThemesSettings is a Querydsl query type for QOrganizationThemesSettings
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizationThemesSettings extends com.querydsl.sql.RelationalPathBase<QOrganizationThemesSettings> {

    private static final long serialVersionUID = 1326732374;

    public static final QOrganizationThemesSettings organizationThemesSettings = new QOrganizationThemesSettings("organization_themes_settings");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath settings = createString("settings");

    public final NumberPath<Integer> themeId = createNumber("themeId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QOrganizationThemesSettings> organizationThemesSettingsPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> organizationThemesSettingsOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QThemes> organizationThemesSettingsThemeIdFkey = createForeignKey(themeId, "id");

    public QOrganizationThemesSettings(String variable) {
        super(QOrganizationThemesSettings.class, forVariable(variable), "public", "organization_themes_settings");
        addMetadata();
    }

    public QOrganizationThemesSettings(String variable, String schema, String table) {
        super(QOrganizationThemesSettings.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizationThemesSettings(String variable, String schema) {
        super(QOrganizationThemesSettings.class, forVariable(variable), schema, "organization_themes_settings");
        addMetadata();
    }

    public QOrganizationThemesSettings(Path<? extends QOrganizationThemesSettings> path) {
        super(path.getType(), path.getMetadata(), "public", "organization_themes_settings");
        addMetadata();
    }

    public QOrganizationThemesSettings(PathMetadata metadata) {
        super(QOrganizationThemesSettings.class, metadata, "public", "organization_themes_settings");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(settings, ColumnMetadata.named("settings").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(themeId, ColumnMetadata.named("theme_id").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

