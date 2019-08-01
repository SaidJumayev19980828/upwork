package com.nasnav.persistence;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "role_employee_users")
@EqualsAndHashCode(callSuper=false)
public class RoleEmployeeUser extends DefaultBusinessEntity<Integer> {


    @Column(name = "employee_user_id")
    @Type(type = "com.nasnav.persistence.mapping.customtypes.IntgerAsLongType")
    private Long employeeUserId;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}
