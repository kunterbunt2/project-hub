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
//        scope = Version.class,
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id")
public class Version extends AbstractTimeAware {

    private Long id;

    private String name;

    @ToString.Exclude//help intellij debugger not to go into a loop
    @JsonBackReference(value = "product-version")
    private Product product;

    @JsonManagedReference(value = "version-project")
    private List<Project> projects = new ArrayList<>();

    public Project addProject(Project project) {
        projects.add(project);
        project.setVersion(this);
        return project;
    }

    String getKey() {
        return "V-" + id;
    }

    public void initialize(List<User> allUsers) {
        projects.forEach(project -> project.initialize(allUsers));
    }
}
