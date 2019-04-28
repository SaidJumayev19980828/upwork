package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.UserDTOs;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "employee_users")
@EqualsAndHashCode(callSuper=false)
public class EmployeeUserEntity extends DefaultBusinessEntity<Integer> {

    @Column(name = "name")
    private String name;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "email")
    private String email;

    @Column(name = "encrypted_password")
    private String encryptedPassword;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_sent_at")
    private LocalDateTime resetPasswordSentAt;

    @Column(name = "remember_created_at")
    private LocalDateTime rememberCreatedAt;

    @Column(name = "sign_in_count")
    private int signInCount;

    @Column(name = "current_sign_in_at")
    private LocalDateTime currentSignInAt;

    @Column(name = "last_sign_in_at")
    private LocalDateTime lastSignInAt;

    @Column(name = "type")
    private String type;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "authentication_token")
    private String authenticationToken;

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

    public EmployeeUserEntity() {
        super();
    }
    
    public static EmployeeUserEntity createEmployeeUser(UserDTOs.EmployeeUserCreationObject employeeUserJson) {
        // parse Json to EmployeeUserEntity
        EmployeeUserEntity employeeUser = new EmployeeUserEntity();
        employeeUser.setName(employeeUserJson.name);
        employeeUser.setEmail(employeeUserJson.email);
        employeeUser.setEncryptedPassword(EntityConstants.INITIAL_PASSWORD);
        employeeUser.setOrganizationId(employeeUserJson.org_id);
        employeeUser.setShopId(employeeUserJson.store_id);
        employeeUser.setCreatedAt(LocalDateTime.now());
        employeeUser.setUpdatedAt(LocalDateTime.now());
        
        return employeeUser;
    }

}
