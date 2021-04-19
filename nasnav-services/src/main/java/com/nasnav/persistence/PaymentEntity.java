package com.nasnav.persistence;

import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.enumerations.TransactionCurrency;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static lombok.AccessLevel.NONE;
import static org.apache.commons.beanutils.BeanUtils.copyProperties;

@Data
@Entity
@Table(name = "payments")
@EqualsAndHashCode(callSuper=false)
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private OrdersEntity ordersEntity;

    @Column(name="operator")
    private String operator;

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

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "meta_order_id")
	private Long metaOrderId;

	@Column(name = "org_payment_id")
	private Integer orgPaymentId;

	@Column(name = "session_id")
	private String sessionId;

	@Transient
    @Setter(NONE)
    private PaymentEntity previousState;
    
    
    
    @PostLoad
    private void setPreviousState(){
        previousState = new PaymentEntity();
        try {
			copyProperties(previousState, this);
		} catch (Throwable e) {
			copyEachField(previousState);
		}
    }
    
    
    
    
	private void copyEachField(PaymentEntity previousState) {
		previousState.setAmount(getAmount());
		previousState.setCurrency(getCurrency());
		previousState.setExecuted(getExecuted());
		previousState.setId(getId());
		previousState.setObject(getObject());
		previousState.setOperator(getOperator());
		previousState.setOrdersEntity(getOrdersEntity());
		previousState.setStatus(getStatus());
		previousState.setUid(getUid());
	}
    
    
    

}
