/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.projecthub.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Product product;

    private Long productId;

    //    @JsonManagedReference(value = "version-project")
    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private List<Project> projects = new ArrayList<>();

    public Project addProject(Project project) {
        projects.add(project);
        project.setVersion(this);
        return project;
    }

    String getKey() {
        return "V-" + id;
    }

    public void initialize(List<User> allUsers, List<Project> allProjects, List<Sprint> allSprints, List<Task> allTasks) {
        allProjects.forEach(project -> {
            if (project.getVersionId() == id) {
                addProject(project);
            }
        });
        projects.forEach(project -> project.initialize(allUsers, allSprints, allTasks));
    }
}
