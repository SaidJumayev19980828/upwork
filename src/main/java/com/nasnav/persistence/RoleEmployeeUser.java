package com.nasnav.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "role_employee_users")
@EqualsAndHashCode(callSuper=false)
public class RoleEmployeeUser extends DefaultBusinessEntity<Integer> {


    @Column(name = "employee_user_id")
    private Long employeeUserId;

    @Column(name = "role_id")
    private Integer roleId;
}
