package com.nasnav.persistence;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.UserDTOs;

import com.nasnav.dto.UserRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@Entity
@Table(name = "employee_users")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class EmployeeUserEntity extends BaseUserEntity {
	
	@Column(name = "name")
	private String name;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "remember_created_at")
    private LocalDateTime rememberCreatedAt;

    @Column(name = "type")
    private String type;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "seo")
    private Boolean seo;

    @Column(name = "following_standards")
    private Boolean followingStandards;

    @Column(name = "service_type")
    private Integer serviceType;

    @Column(name = "tutorial")
    private Boolean tutorial;

    @Column(name = "shop_id")
    private Long shopId;

    @Column(name = "organization_manager_id")
    private Long organizationManagerId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "avatar")
    private String avatar;

    public static EmployeeUserEntity createEmployeeUser(UserDTOs.EmployeeUserCreationObject employeeUserJson) {
        // parse Json to EmployeeUserEntity
        EmployeeUserEntity employeeUser = new EmployeeUserEntity();
        employeeUser.setName(employeeUserJson.name);
        employeeUser.setEmail(employeeUserJson.email);
        employeeUser.setEncryptedPassword(EntityConstants.INITIAL_PASSWORD);
        employeeUser.setOrganizationId(employeeUserJson.orgId);
        employeeUser.setShopId(employeeUserJson.storeId);
        employeeUser.setCreatedAt(LocalDateTime.now());
        employeeUser.setUpdatedAt(LocalDateTime.now());
        employeeUser.setAvatar(employeeUserJson.getAvatar());
        return employeeUser;
    }

    @Override
    public UserRepresentationObject getRepresentation() {
        UserRepresentationObject obj = new UserRepresentationObject();
        BeanUtils.copyProperties(this, obj);
        obj.setImage(this.avatar);
        obj.id = this.getId();
        return obj;
    }
}
