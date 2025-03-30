package de.bushnaq.abdalla.projecthub.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
        scope = Product.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Product extends AbstractTimeAware {

    private Long id;

    private String name;

    //    @JsonManagedReference(value = "product-version")
    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private List<Version> versions = new ArrayList<>();

    public void addVersion(Version version) {
        versions.add(version);
        version.setProduct(this);
    }

    public void initialize(List<User> allUsers, List<Version> allVersions, List<Project> allProjects, List<Sprint> allSprints, List<Task> allTasks) {
        allVersions.forEach(version -> {
            if (version.getProductId() == id) {
                addVersion(version);
            }
        });
        versions.forEach(version -> version.initialize(allUsers, allProjects, allSprints, allTasks));
    }
}
