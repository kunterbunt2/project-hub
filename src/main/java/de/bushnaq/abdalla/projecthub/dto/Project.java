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
//        scope = Project.class,
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id")
public class Project extends AbstractTimeAware {
    private Long   id;
    private String name;
    private String requester;

    //    @JsonManagedReference(value = "project-sprint")
    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private List<Sprint> sprints = new ArrayList<>();

    //    @JsonBackReference(value = "version-project")
//    @ToString.Exclude//help intellij debugger not to go into a loop
    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Version version;

    private Long versionId;

    public void addSprint(Sprint sprint) {
        sprints.add(sprint);
    }

    public void initialize(List<User> allUsers, List<Sprint> allSprints, List<Task> allTasks) {
        allSprints.forEach(sprint -> {
            if (sprint.getProjectId() == id) {
                addSprint(sprint);
            }
        });
        sprints.forEach(sprint -> sprint.initialize(allUsers, allTasks));
    }
}