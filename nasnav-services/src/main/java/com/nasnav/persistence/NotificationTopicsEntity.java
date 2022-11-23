package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "notification_topics")
@Data
public class NotificationTopicsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String topic;


    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinTable(name = "topic_employee_users"
            ,joinColumns = {@JoinColumn(name="topic_id")}
            ,inverseJoinColumns = {@JoinColumn(name="employee_user_id")})
    private Set<EmployeeUserEntity> employees;

}
