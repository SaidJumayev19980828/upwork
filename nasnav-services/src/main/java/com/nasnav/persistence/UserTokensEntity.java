package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Optional.ofNullable;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity userEntity;


    @ManyToOne(fetch = FetchType.LAZY)
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
