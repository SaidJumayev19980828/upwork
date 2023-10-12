package com.nasnav.persistence;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "rocket_chat_employee_agents")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class RocketChatEmployeeAgentEntity {
	@Id
	private Long employeeId;
	// rocket chat user id is the same as agent id
	private String agentId;

	@MapsId
	@OneToOne(optional = false)
	@JoinColumn(name = "employee_id", referencedColumnName = "id")
	private EmployeeUserEntity employee;

	public RocketChatEmployeeAgentEntity(String agentId, EmployeeUserEntity employee) {
		this.agentId = agentId;
		this.employee = employee;
	}
}
