package com.nasnav.persistence;


import com.nasnav.response.ResponseStatus;
import com.nasnav.response.exception.EntityValidationException;
import lombok.Data;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "users_entity")
@Data
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotEmpty(message = "Name is required")
    @Pattern(regexp = EntityConstants.Name_FIELD_PATTEREN, message = "Provided name is invalid")
    private String name;

    @NotEmpty(message = "Email is required")
    @Email(message = "Provided email is invalid")
    @Column(unique = true)
    // we can also use this pattern if the default java Email validator is not ok.
//    @Pattern(regexp = EntityConstants.EMAIL_PATTEREN, message = "Provided email is invalid")
    private String email;

    private Long status;

    public User() {
        this.id = null;
    }

    @PrePersist
    public void validatePrePersist() {
        this.validateUserEntity();
    }

    /**
     * Validate User entity
     *
     * @throws EntityValidationException if current entity is not valid
     */
    private void validateUserEntity() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(this);
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            EntityValidationException userViolationException = new EntityValidationException("Invalid User Entity");
            for (ConstraintViolation<User> violation : constraintViolations) {
                userViolationException.addResponseStatus(getResponseStatus(violation));
            }
            throw userViolationException;
        }
    }

    /**
     * @param violation ConstraintViolation to be proccessed
     * @return error response status according to invalid field
     */
    private ResponseStatus getResponseStatus(ConstraintViolation<User> violation) {
        if ("email".equals(violation.getPropertyPath().toString())) {
            return (ResponseStatus.INVALID_EMAIL);
        }
        if ("name".equals(violation.getPropertyPath().toString())) {
            return (ResponseStatus.INVALID_NAME);
        }
        // return default Response Status
        return (ResponseStatus.SYS_ERROR);
    }
}
