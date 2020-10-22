package com.nasnav.persistence;

import java.time.LocalDateTime;

import javax.persistence.*;

import com.nasnav.dto.UserRepresentationObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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

    @Override
    public UserRepresentationObject getRepresentation() {
        UserRepresentationObject obj = new UserRepresentationObject();
        BeanUtils.copyProperties(this, obj);
        obj.setImage(this.avatar);
        obj.id = this.getId();

        return obj;
    }
}
