package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "rocket_chat_customer_tokens")
@EqualsAndHashCode(callSuper=false)
public class RocketChatCustomerTokenEntity extends DefaultBusinessEntity<Integer> {
	@OneToOne(optional = false)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private UserEntity user;

	@Column(name = "token")
	@NotNull
	private String token;

	public RocketChatCustomerTokenEntity(String token) {
		this.token = token;
	}
}