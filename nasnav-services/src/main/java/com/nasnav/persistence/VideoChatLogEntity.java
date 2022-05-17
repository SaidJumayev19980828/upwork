package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.VideoChatLogRepresentationObject;
import com.nasnav.enumerations.VideoChatStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_chat_logs")
@Data
public class VideoChatLogEntity implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private String name;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EmployeeUserEntity assignedTo;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrganizationEntity organization;

    @Column(name = "is_active")
    private Boolean isActive;

    private Integer status;

    private String description;


    @Override
    public BaseRepresentationObject getRepresentation() {
        var obj = new VideoChatLogRepresentationObject();
        obj.setId(getId());
        obj.setName(name);
        obj.setOrganizationName(organization.getName());
        obj.setAssignedToName(assignedTo.getName());
        obj.setUserName(user.getName());
        obj.setIsActive(isActive);
        obj.setDescription(description);

        VideoChatStatus
                .getVideoChatState(status)
                .ifPresent(obj::setStatus);
        return obj;
    }


}
