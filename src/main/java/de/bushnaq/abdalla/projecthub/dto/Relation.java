package de.bushnaq.abdalla.projecthub.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Relation {

    Long id;
    Long predecessorId;

    public Relation(Task dependency) {
        predecessorId = dependency.getId();
    }
}
