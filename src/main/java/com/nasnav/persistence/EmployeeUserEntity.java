package com.nasnav.persistence;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.UserDTOs;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

    
    
    
    //TODO: this is a work around because the column type of employee_users.id is int4 which is not converted into Long
    //by hibernate
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Type(type = "com.nasnav.persistence.mapping.customtypes.IntgerAsLongType")
    private Long id;
    

    
    
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
