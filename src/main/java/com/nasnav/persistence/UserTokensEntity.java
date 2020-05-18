package com.nasnav.persistence;

import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "user_tokens")
@Data
public class UserTokensEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "update_time", nullable = false, length = 29)
    @UpdateTimestamp
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity userEntity;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_user_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EmployeeUserEntity employeeUserEntity;
    
    
    
    public Optional<BaseUserEntity> getBaseUser() {
    	if (userEntity != null) {
			return ofNullable(userEntity);
		}

		return ofNullable(employeeUserEntity);
    }
}
