package de.bushnaq.abdalla.projecthub.db;

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
public abstract class AbstractTimeAwareEntity {
    private OffsetDateTime created;
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
