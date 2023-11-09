package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "post_attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAttachmentsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    private String type;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "post_id", nullable = false,referencedColumnName = "id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PostEntity post;


    public static PostAttachmentsEntity buildAttachment(String url, String type) {
        return PostAttachmentsEntity.builder().url(url).type(type).build();
    }
}
