package com.nasnav.querydsl.sql;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;

@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProductAddons extends com.querydsl.sql.RelationalPathBase<QProductAddons> {
	private static final long serialVersionUID = 1L;

	public static final QProductAddons productAddons = new QProductAddons("product_addons");
	public final NumberPath<Long> productId = createNumber("productId", Long.class);

	public final NumberPath<Long> addonId = createNumber("addonId", Long.class);

	public final com.querydsl.sql.ForeignKey<QProducts> productAddonsProductIdFkey = createForeignKey(productId, "id");

	public final com.querydsl.sql.ForeignKey<QAddons> productAddonsAddonIdFkey = createForeignKey(addonId, "id");

	/**
		 * 
		 */

	public QProductAddons(String variable) {
		super(QProductAddons.class, forVariable(variable), "public", "product_addons");
		addMetadata();
	}

	public QProductAddons(String variable, String schema, String table) {
		super(QProductAddons.class, forVariable(variable), schema, table);
		addMetadata();
	}

	public QProductAddons(String variable, String schema) {
		super(QProductAddons.class, forVariable(variable), schema, "product_addons");
		addMetadata();
	}

	public QProductAddons(Path<? extends QProductAddons> path) {
		super(path.getType(), path.getMetadata(), "public", "product_addons");
		addMetadata();
	}

	public QProductAddons(PathMetadata metadata) {
		super(QProductAddons.class, metadata, "public", "product_addons");
		addMetadata();
	}

	public void addMetadata() {
		addMetadata(productId,
	     ColumnMetadata.named("product_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
		addMetadata(addonId, ColumnMetadata.named("addon_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
	}

}
