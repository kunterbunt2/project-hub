package de.bushnaq.abdalla.projecthub.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
//@JsonIdentityInfo(
//        scope = Project.class,
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id")
public class Project extends AbstractTimeAware {
    private Long   id;
    private String name;
    private String requester;

    @JsonManagedReference(value = "project-sprint")
    private List<Sprint> sprints = new ArrayList<>();

    @JsonBackReference(value = "version-project")
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Version version;

    public void addSprint(Sprint sprint) {
        sprints.add(sprint);
    }
}