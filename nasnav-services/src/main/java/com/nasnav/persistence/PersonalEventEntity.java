package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "personal_event")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PersonalEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Column(nullable = false)
    private boolean canceled;

    @Column(nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private EmployeeUserEntity employee;


    @OneToMany(mappedBy = "personalEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<EventInviteeEntity> invitees = new HashSet<>();



    public void addInvitee(EventInviteeEntity invitee) {
        if (invitee != null) {
            if (isDuplicateInvitee(invitee)) {
                return;
            }
            getInvitees().add(invitee);
            invitee.setPersonalEvent(this);
        }
    }

    private boolean isDuplicateInvitee(EventInviteeEntity newInvitee) {
        return getInvitees().stream()
                .anyMatch(existingInvitee ->
                        (existingInvitee.getUser() != null && existingInvitee.getUser().equals(newInvitee.getUser())) ||
                                (existingInvitee.getEmployee() != null && existingInvitee.getEmployee().equals(newInvitee.getEmployee()))
                );
    }

}

