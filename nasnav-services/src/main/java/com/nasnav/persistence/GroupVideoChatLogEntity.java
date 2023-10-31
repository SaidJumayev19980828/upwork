package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.GroupVideoChatLogRepresentationObject;
import com.nasnav.enumerations.VideoChatStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "group_video_chat_logs")
@Data
public class GroupVideoChatLogEntity implements BaseEntity {
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

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "group_video_chat_log_user"
            ,joinColumns = {@JoinColumn(name="group_video_chat_log_id")}
            ,inverseJoinColumns = {@JoinColumn(name="user_id")})
    private List<UserEntity> users;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "group_video_chat_log_employee_user"
            ,joinColumns = {@JoinColumn(name="group_video_chat_log_id")}
            ,inverseJoinColumns = {@JoinColumn(name="employee_user_id")})
    private List<EmployeeUserEntity> employeeUsers;


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
        var obj = new GroupVideoChatLogRepresentationObject();
        obj.setId(getId());
        obj.setName(name);
        obj.setOrganizationId(organization.getId());
        obj.setOrganizationName(organization.getName());
        if(!employeeUsers.isEmpty()) {
            List<Long> employeeUserIds = employeeUsers.stream().map(employeeUserEntity -> employeeUserEntity.getId()).collect(Collectors.toList());
            obj.setEmployeeUserIds(employeeUserIds);
        }
        if(getShop() != null) {
            obj.setShopId(getShop().getId());
            obj.setShopName(getShop().getName());
        }
        if(!users.isEmpty()) {
            List<Long> employeeUserIds = users.stream().map(user -> user.getId()).collect(Collectors.toList());
            obj.setEmployeeUserIds(employeeUserIds);
        }
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
