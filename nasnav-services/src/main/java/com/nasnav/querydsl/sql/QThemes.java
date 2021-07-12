package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QThemes is a Querydsl query type for QThemes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QThemes extends com.querydsl.sql.RelationalPathBase<QThemes> {

    private static final long serialVersionUID = -998231808;

    public static final QThemes themes = new QThemes("themes");

    public final StringPath defaultSettings = createString("defaultSettings");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath image = createString("image");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> themeClassId = createNumber("themeClassId", Integer.class);

    public final StringPath uid = createString("uid");

    public final com.querydsl.sql.PrimaryKey<QThemes> themesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QThemeClasses> themesThemeClassIdFkey = createForeignKey(themeClassId, "id");

    public final com.querydsl.sql.ForeignKey<QOrganizationThemesSettings> _organizationThemesSettingsThemeIdFkey = createInvForeignKey(id, "theme_id");

    public QThemes(String variable) {
        super(QThemes.class, forVariable(variable), "public", "themes");
        addMetadata();
    }

    public QThemes(String variable, String schema, String table) {
        super(QThemes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QThemes(String variable, String schema) {
        super(QThemes.class, forVariable(variable), schema, "themes");
        addMetadata();
    }

    public QThemes(Path<? extends QThemes> path) {
        super(path.getType(), path.getMetadata(), "public", "themes");
        addMetadata();
    }

    public QThemes(PathMetadata metadata) {
        super(QThemes.class, metadata, "public", "themes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(defaultSettings, ColumnMetadata.named("default_settings").withIndex(5).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(image, ColumnMetadata.named("image").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(themeClassId, ColumnMetadata.named("theme_class_id").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(uid, ColumnMetadata.named("uid").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
    }

}

