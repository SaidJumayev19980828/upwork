package com.nasnav.querydsl.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.nasnav.persistence.OrganizationPaymentGatewaysEntity;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrganizationPaymentGatewaysEntity is a Querydsl query type for OrganizationPaymentGatewaysEntity
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QOrganizationPaymentGatewaysEntity extends EntityPathBase<OrganizationPaymentGatewaysEntity> {

    private static final long serialVersionUID = 1913535384L;

    public static final QOrganizationPaymentGatewaysEntity organizationPaymentGatewaysEntity = new QOrganizationPaymentGatewaysEntity("organizationPaymentGatewaysEntity");

    public final StringPath account = createString("account");

    public final StringPath gateway = createString("gateway");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Long> organizationId = createNumber("organizationId", Long.class);

    public QOrganizationPaymentGatewaysEntity(String variable) {
        super(OrganizationPaymentGatewaysEntity.class, forVariable(variable));
    }

    public QOrganizationPaymentGatewaysEntity(Path<? extends OrganizationPaymentGatewaysEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrganizationPaymentGatewaysEntity(PathMetadata metadata) {
        super(OrganizationPaymentGatewaysEntity.class, metadata);
    }

}

