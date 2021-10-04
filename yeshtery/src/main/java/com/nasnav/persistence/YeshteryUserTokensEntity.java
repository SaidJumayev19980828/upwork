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
@Table(name = "yeshtery_user_tokens")
@Data
public class YeshteryUserTokensEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "update_time", nullable = false, length = 29)
    @UpdateTimestamp
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yeshtery_user_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private YeshteryUserEntity yeshteryUserEntity;

    public Optional<BaseYeshteryUserEntity> getBaseYeshteryUser() {
        if (yeshteryUserEntity != null) {
            return ofNullable(yeshteryUserEntity);
        }
        return null;
    }

}
