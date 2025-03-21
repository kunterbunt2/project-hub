package de.bushnaq.abdalla.projecthub.dao;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@MappedSuperclass
@Getter
@Setter
@ToString
public abstract class AbstractTimeAwareDTO {

    @Column(nullable = false)
    private OffsetDateTime created;

    @Column(nullable = false)
    private OffsetDateTime updated;

    @PrePersist
    protected void onCreate() {
        created = OffsetDateTime.now();
        updated = created;
    }

    @PreUpdate
    protected void onUpdate() {
        updated = OffsetDateTime.now();
    }

}
