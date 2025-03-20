package de.bushnaq.abdalla.projecthub.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Project extends AbstractTimeAware {
    private Long         id;
    private String       name;
    private String       requester;
    private List<Sprint> sprints = new ArrayList<>();

    public void addSprint(Sprint sprint) {
        sprints.add(sprint);
    }
}