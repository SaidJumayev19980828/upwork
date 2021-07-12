package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOrganizationThemes is a Querydsl query type for QOrganizationThemes
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOrganizationThemes extends com.querydsl.sql.RelationalPathBase<QOrganizationThemes> {

    private static final long serialVersionUID = -2037778669;

    public static final QOrganizationThemes organizationThemes = new QOrganizationThemes("organization_themes");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath firstColor = createString("firstColor");

    public final BooleanPath firstSection = createBoolean("firstSection");

    public final StringPath firstSectionImage = createString("firstSectionImage");

    public final NumberPath<Integer> firstSectionProduct = createNumber("firstSectionProduct", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath logo = createString("logo");

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final StringPath secondColor = createString("secondColor");

    public final BooleanPath secondSection = createBoolean("secondSection");

    public final StringPath secondSectionImage = createString("secondSectionImage");

    public final NumberPath<Integer> secondSectionProduct = createNumber("secondSectionProduct", Integer.class);

    public final BooleanPath sliderBody = createBoolean("sliderBody");

    public final StringPath sliderHeader = createString("sliderHeader");

    public final SimplePath<String[]> sliderImages = createSimple("sliderImages", String[].class);

    public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<QOrganizationThemes> organizationThemesPkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> rails66b5304bc3Fk = createForeignKey(organizationId, "id");

    public QOrganizationThemes(String variable) {
        super(QOrganizationThemes.class, forVariable(variable), "public", "organization_themes");
        addMetadata();
    }

    public QOrganizationThemes(String variable, String schema, String table) {
        super(QOrganizationThemes.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOrganizationThemes(String variable, String schema) {
        super(QOrganizationThemes.class, forVariable(variable), schema, "organization_themes");
        addMetadata();
    }

    public QOrganizationThemes(Path<? extends QOrganizationThemes> path) {
        super(path.getType(), path.getMetadata(), "public", "organization_themes");
        addMetadata();
    }

    public QOrganizationThemes(PathMetadata metadata) {
        super(QOrganizationThemes.class, metadata, "public", "organization_themes");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(14).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(firstColor, ColumnMetadata.named("first_color").withIndex(2).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(firstSection, ColumnMetadata.named("first_section").withIndex(4).ofType(Types.BIT).withSize(1));
        addMetadata(firstSectionImage, ColumnMetadata.named("first_section_image").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(firstSectionProduct, ColumnMetadata.named("first_section_product").withIndex(5).ofType(Types.INTEGER).withSize(10));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(logo, ColumnMetadata.named("logo").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(16).ofType(Types.BIGINT).withSize(19));
        addMetadata(secondColor, ColumnMetadata.named("second_color").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(secondSection, ColumnMetadata.named("second_section").withIndex(8).ofType(Types.BIT).withSize(1));
        addMetadata(secondSectionImage, ColumnMetadata.named("second_section_image").withIndex(10).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(secondSectionProduct, ColumnMetadata.named("second_section_product").withIndex(9).ofType(Types.INTEGER).withSize(10));
        addMetadata(sliderBody, ColumnMetadata.named("slider_body").withIndex(11).ofType(Types.BIT).withSize(1));
        addMetadata(sliderHeader, ColumnMetadata.named("slider_header").withIndex(12).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(sliderImages, ColumnMetadata.named("slider_images").withIndex(13).ofType(Types.ARRAY).withSize(2147483647));
        addMetadata(updatedAt, ColumnMetadata.named("updated_at").withIndex(15).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
    }

}

