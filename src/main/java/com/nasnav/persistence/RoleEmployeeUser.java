package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author ahmed.bastawesy
 */
@Data
@Entity
@Table(name = "role_employee_users")
public class RoleEmployeeUser extends DefaultBusinessEntity<Integer> {


    @Column(name = "employee_user_id")
    private Integer employeeUserId;

    @Column(name = "role_id")
    private Integer roleId;
}
