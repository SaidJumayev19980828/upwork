package com.nasnav.persistence;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.response.ApiLogsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

import static java.util.Optional.ofNullable;

@Data
@Entity
@Table(name = "api_logs")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class ApiLogsEntity implements BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "url")
	private String url;

	@Column(name = "call_date")
	@CreationTimestamp
	private LocalDateTime callDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", referencedColumnName = "id")
	private UserEntity loggedCustomer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", referencedColumnName = "id")
	private EmployeeUserEntity loggedEmployee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", referencedColumnName = "id")
	private OrganizationEntity organization;

	@Column(name = "request_content")
	private String requestContent;

	@Column(name = "response_code")
	private Integer responseCode;

	@Override
	public BaseRepresentationObject getRepresentation() {
		ApiLogsDTO dto = new ApiLogsDTO();

		dto.setId(id);
		dto.setUrl(url);
		dto.setCallDate(callDate.toString());

		dto.setOrganizationId(organization.getId());
		dto.setRequestContent(requestContent);
		dto.setResponseCode(responseCode);

		ofNullable(loggedCustomer)
				.map(UserEntity::getId)
				.ifPresent(dto::setCustomerId);

		ofNullable(loggedEmployee)
				.map(EmployeeUserEntity::getId)
				.ifPresent(dto::setEmployeeId);

		return dto;
	}
}