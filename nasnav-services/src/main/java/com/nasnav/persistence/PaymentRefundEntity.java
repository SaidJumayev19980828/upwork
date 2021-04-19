package com.nasnav.persistence;

import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.enumerations.TransactionCurrency;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "payment_refunds")
@EqualsAndHashCode(callSuper=false)
public class PaymentRefundEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    private PaymentEntity paymentEntity;

    @Column(name = "uid")
    private String uid;

    @Column(name="status")
    private int status;

    public PaymentStatus getStatus() {
        return PaymentStatus.getPaymentStatus(this.status);
    }
    public void setStatus(PaymentStatus status) {
        this.status = status.getValue();
    }

    @Column(name="executed")
    private Date executed;

    @Column(name="amount", precision=10, scale=2)
    private BigDecimal amount;

    @Column(name="currency")
    private Integer currency;

    public TransactionCurrency getCurrency() { return TransactionCurrency.getTransactionCurrency(this.currency); }
    public void setCurrency(TransactionCurrency currency) { this.currency = currency.getValue(); }

    @Column(name = "object")
    private String object;


}
