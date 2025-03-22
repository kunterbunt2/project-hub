package de.bushnaq.abdalla.projecthub.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@JsonIdentityInfo(
        scope = Project.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Project extends AbstractTimeAware {
    private Long         id;
    private String       name;
    private String       requester;
    private List<Sprint> sprints = new ArrayList<>();

    //    @JsonBackReference
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Version version;

    public void addSprint(Sprint sprint) {
        sprints.add(sprint);
    }
}