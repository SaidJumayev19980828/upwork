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

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ShopsEntity shop;

    @Column(name = "is_active")
    private Boolean isActive;

    private Integer status;

    private String description;


    @Override
    public BaseRepresentationObject getRepresentation() {
        var obj = new VideoChatLogRepresentationObject();
        obj.setId(getId());
        obj.setName(name);
        obj.setOrganizationId(organization.getId());
        obj.setOrganizationName(organization.getName());
        if(assignedTo != null) {
            obj.setAssignedToId(assignedTo.getId());
            obj.setAssignedToName(assignedTo.getName());
        }
        if(getShop() != null) {
            obj.setShopId(getShop().getId());
            obj.setShopName(getShop().getName());
        }
        obj.setUserId(user.getId());
        obj.setUserName(user.getName());
        obj.setUserEmail(user.getEmail());
        obj.setIsActive(isActive);
        obj.setDescription(description);
        obj.setCreatedAt(getCreatedAt());
        obj.setEndedAt(endedAt);

        VideoChatStatus
                .getVideoChatState(status)
                .ifPresent(obj::setStatus);
        return obj;
    }

    public void addDescription(String text){
        if(description == null)
            description = "";

        description += text + " at " + LocalDateTime.now() + ".\n";
    }

}
