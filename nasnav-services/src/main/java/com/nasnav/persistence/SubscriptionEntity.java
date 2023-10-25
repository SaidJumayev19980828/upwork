package com.nasnav.persistence;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Table(name = "subscription")
@EqualsAndHashCode(callSuper=false)
@Entity
@Data
public class SubscriptionEntity extends DefaultBusinessEntity<Long> implements Serializable {

    @Column(name="type")
    private String type;

    @Column(name="payment_date")
    private LocalDateTime paymentDate;

    @Column(name="paid_amount")
    private BigDecimal paidAmount;

    @Column(name="start_date")
    private Date startDate;

    @Column(name="expiration_date")
    private Date expirationDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "package_id", referencedColumnName = "id")
    private PackageEntity packageEntity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private OrganizationEntity organization;

    @Column(name="status")
    private String status;

    @Column(name="stripe_subscription_id")
    private String stripeSubscriptionId;
}
