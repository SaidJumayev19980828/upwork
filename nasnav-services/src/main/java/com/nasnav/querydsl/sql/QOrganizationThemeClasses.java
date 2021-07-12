package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganizationThemeClasses is a Querydsl query type for QOrganizationThemeClasses
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizationThemeClasses extends com.querydsl.sql.RelationalPathBase<QOrganizationThemeClasses> {

    private static final long serialVersionUID = -1632088410;

    public static final QOrganizationThemeClasses organizationThemeClasses = new QOrganizationThemeClasses("organization_theme_classes");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final NumberPath<Integer> themeClassId = createNumber("themeClassId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QOrganizationThemeClasses> organizationThemeClassesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> organizationThemeClassesOrganizationIdFkey = createForeignKey(organizationId, "id");

    public final com.querydsl.sql.ForeignKey<QThemeClasses> organizationThemeClassesThemeClassIdFkey = createForeignKey(themeClassId, "id");

    public QOrganizationThemeClasses(String variable) {
        super(QOrganizationThemeClasses.class, forVariable(variable), "public", "organization_theme_classes");
        addMetadata();
    }

    public QOrganizationThemeClasses(String variable, String schema, String table) {
        super(QOrganizationThemeClasses.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizationThemeClasses(String variable, String schema) {
        super(QOrganizationThemeClasses.class, forVariable(variable), schema, "organization_theme_classes");
        addMetadata();
    }

    public QOrganizationThemeClasses(Path<? extends QOrganizationThemeClasses> path) {
        super(path.getType(), path.getMetadata(), "public", "organization_theme_classes");
        addMetadata();
    }

    public QOrganizationThemeClasses(PathMetadata metadata) {
        super(QOrganizationThemeClasses.class, metadata, "public", "organization_theme_classes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(themeClassId, ColumnMetadata.named("theme_class_id").withIndex(3).ofType(Types.INTEGER).withSize(10).notNull());
    }

}

