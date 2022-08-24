package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "role_employee_users")
@EqualsAndHashCode(callSuper=false)
public class RoleEmployeeUser extends DefaultBusinessEntity<Integer> {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_user_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private EmployeeUserEntity employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Role role;
}
