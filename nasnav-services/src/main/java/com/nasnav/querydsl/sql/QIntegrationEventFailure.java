package com.nasnav.querydsl.sql;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;

import javax.annotation.Generated;
import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;




/**
 * QIntegrationEventFailure is a Querydsl query type for QIntegrationEventFailure
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QIntegrationEventFailure extends com.querydsl.sql.RelationalPathBase<QIntegrationEventFailure> {

    private static final long serialVersionUID = -1774559607;

    public static final QIntegrationEventFailure integrationEventFailure = new QIntegrationEventFailure("integration_event_failure");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath eventData = createString("eventData");

    public final StringPath eventType = createString("eventType");

    public final StringPath fallbackException = createString("fallbackException");

    public final StringPath handleException = createString("handleException");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QIntegrationEventFailure> integrationEventFailurePkey = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QOrganizations> integrationEventFailureOrganizationIdFkey = createForeignKey(organizationId, "id");

    public QIntegrationEventFailure(String variable) {
        super(QIntegrationEventFailure.class, forVariable(variable), "public", "integration_event_failure");
        addMetadata();
    }

    public QIntegrationEventFailure(String variable, String schema, String table) {
        super(QIntegrationEventFailure.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QIntegrationEventFailure(String variable, String schema) {
        super(QIntegrationEventFailure.class, forVariable(variable), schema, "integration_event_failure");
        addMetadata();
    }

    public QIntegrationEventFailure(Path<? extends QIntegrationEventFailure> path) {
        super(path.getType(), path.getMetadata(), "public", "integration_event_failure");
        addMetadata();
    }

    public QIntegrationEventFailure(PathMetadata metadata) {
        super(QIntegrationEventFailure.class, metadata, "public", "integration_event_failure");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(5).ofType(Types.TIMESTAMP).withSize(29).withDigits(6).notNull());
        addMetadata(eventData, ColumnMetadata.named("event_data").withIndex(4).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(eventType, ColumnMetadata.named("event_type").withIndex(3).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(fallbackException, ColumnMetadata.named("fallback_exception").withIndex(7).ofType(Types.VARCHAR).withSize(2147483647));
        addMetadata(handleException, ColumnMetadata.named("handle_exception").withIndex(6).ofType(Types.VARCHAR).withSize(2147483647).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(organizationId, ColumnMetadata.named("organization_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

