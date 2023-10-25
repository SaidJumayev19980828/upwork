package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Table(name = "stripe_customer")
@EqualsAndHashCode(callSuper=false)
@Entity
@Data
public class StripeCustomerEntity extends DefaultBusinessEntity<Long> implements Serializable {

    @Column(name="customer_id")
    private String customerId;

    @OneToOne(optional = false)
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private OrganizationEntity organization;
}
