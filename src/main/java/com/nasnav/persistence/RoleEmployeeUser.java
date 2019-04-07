package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "role_employee_users")
@EqualsAndHashCode(callSuper=false)
public class RoleEmployeeUser extends DefaultBusinessEntity<Integer> {


    @Column(name = "employee_user_id")
    private Integer employeeUserId;

    @Column(name = "role_id")
    private Integer roleId;
}
