package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "event_attachments")
@Data
public class EventAttachmentsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    private String type;

    @Column(name = "coin")
    private Long coin;



    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "event_id", nullable = false,referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EventEntity event;
}
