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
        scope = Version.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Version extends AbstractTimeAware {

    private Long id;

    private String name;

    @ToString.Exclude//help intellij debugger not to go into a loop
    private Product product;

    //    @JsonManagedReference
    private List<Project> projects = new ArrayList<>();

    public Project addProject(Project project) {
        projects.add(project);
        project.setVersion(this);
        return project;
    }

    String getKey() {
        return "V-" + id;
    }

}
