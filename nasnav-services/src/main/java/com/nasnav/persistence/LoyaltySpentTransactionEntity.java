package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Data
@Entity
@Table(name = "loyalty_spent_transactions")
@EqualsAndHashCode(callSuper=false)
public class LoyaltySpentTransactionEntity {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name="transaction_id", referencedColumnName = "id")
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	@lombok.ToString.Exclude
	private LoyaltyPointTransactionEntity transaction;

	@OneToOne(fetch = LAZY)
	@JoinColumn(name="reverse_transaction_id", referencedColumnName = "id")
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	@lombok.ToString.Exclude
	private LoyaltyPointTransactionEntity reverseTransaction;

	@OneToOne(fetch = LAZY)
	@JoinColumn(name="meta_order_id", referencedColumnName = "id")
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	@lombok.ToString.Exclude
	private MetaOrderEntity metaOrder;
}
