package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.ShopsOpeningTimesEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopsOpeningTimesEntity is a Querydsl query type for ShopsOpeningTimesEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QShopsOpeningTimesEntity extends EntityPathBase<ShopsOpeningTimesEntity> {

    private static final long serialVersionUID = -1946132191L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopsOpeningTimesEntity shopsOpeningTimesEntity = new QShopsOpeningTimesEntity("shopsOpeningTimesEntity");

    public final DateTimePath<java.sql.Timestamp> closes = createDateTime("closes", java.sql.Timestamp.class);

    public final NumberPath<Integer> dayOfWeek = createNumber("dayOfWeek", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.sql.Timestamp> opens = createDateTime("opens", java.sql.Timestamp.class);

    public final QShopsEntity shopsEntity;

    public final DatePath<java.sql.Date> validFrom = createDate("validFrom", java.sql.Date.class);

    public final DatePath<java.sql.Date> validThrough = createDate("validThrough", java.sql.Date.class);

    public QShopsOpeningTimesEntity(String variable) {
        this(ShopsOpeningTimesEntity.class, forVariable(variable), INITS);
    }

    public QShopsOpeningTimesEntity(Path<? extends ShopsOpeningTimesEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopsOpeningTimesEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopsOpeningTimesEntity(PathMetadata metadata, PathInits inits) {
        this(ShopsOpeningTimesEntity.class, metadata, inits);
    }

    public QShopsOpeningTimesEntity(Class<? extends ShopsOpeningTimesEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shopsEntity = inits.isInitialized("shopsEntity") ? new QShopsEntity(forProperty("shopsEntity"), inits.get("shopsEntity")) : null;
    }

}

