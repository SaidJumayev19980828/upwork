package com.nasnav.persistence;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.UserDTOs;

import com.nasnav.dto.UserRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

@Data
@Entity
@Table(name = "employee_users")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class EmployeeUserEntity extends BaseUserEntity {
	
	@Column(name = "name")
	private String name;

    @Column(name = "remember_created_at")
    private LocalDateTime rememberCreatedAt;

    @Column(name = "created_by")
    private Integer createdBy;

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
