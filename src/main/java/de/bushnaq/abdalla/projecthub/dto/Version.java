package de.bushnaq.abdalla.projecthub.dto;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Version extends AbstractTimeAware {

    private Long          id;
    private String        name;
    private List<Project> projects = new ArrayList<>();

    public Project addProject(Project project) {
        projects.add(project);
        return project;
    }

    String getKey() {
        return "V-" + id;
    }

}
