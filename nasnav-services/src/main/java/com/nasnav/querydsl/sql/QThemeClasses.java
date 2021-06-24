package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QThemeClasses is a Querydsl query type for QThemeClasses
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QThemeClasses extends com.querydsl.sql.RelationalPathBase<QThemeClasses> {

    private static final long serialVersionUID = 1216651987;

    public static final QThemeClasses themeClasses = new QThemeClasses("theme_classes");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QThemeClasses> themeClassesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizationThemeClasses> _organizationThemeClassesThemeClassIdFkey = createInvForeignKey(id, "theme_class_id");

    public final com.querydsl.sql.ForeignKey<QThemes> _themesThemeClassIdFkey = createInvForeignKey(id, "theme_class_id");

    public QThemeClasses(String variable) {
        super(QThemeClasses.class, forVariable(variable), "public", "theme_classes");
        addMetadata();
    }

    public QThemeClasses(String variable, String schema, String table) {
        super(QThemeClasses.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QThemeClasses(String variable, String schema) {
        super(QThemeClasses.class, forVariable(variable), schema, "theme_classes");
        addMetadata();
    }

    public QThemeClasses(Path<? extends QThemeClasses> path) {
        super(path.getType(), path.getMetadata(), "public", "theme_classes");
        addMetadata();
    }

    public QThemeClasses(PathMetadata metadata) {
        super(QThemeClasses.class, metadata, "public", "theme_classes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647).notNull());
    }

}

