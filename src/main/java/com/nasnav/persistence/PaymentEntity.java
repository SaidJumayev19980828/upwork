package com.nasnav.persistence;

import static lombok.AccessLevel.NONE;
import static org.apache.commons.beanutils.BeanUtils.copyProperties;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.persistence.listeners.PaymentEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@Entity
@EntityListeners(PaymentEntityListener.class)
@Table(name = "payments")
@EqualsAndHashCode(callSuper=false)
public class PaymentEntity extends DefaultBusinessEntity<Long> {

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
