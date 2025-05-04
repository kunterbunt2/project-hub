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
import de.bushnaq.abdalla.projecthub.report.gantt.GanttContext;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Version extends AbstractTimeAware implements Comparable<Version> {

    private Long id;

    private String name;

    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Product product;

    private Long productId;

    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private List<Project> projects = new ArrayList<>();

    public Project addProject(Project project) {
        projects.add(project);
        project.setVersion(this);
        return project;
    }

    @Override
    public int compareTo(Version other) {
        return this.id.compareTo(other.id);
    }

    String getKey() {
        return "V-" + id;
    }

    public void initialize(GanttContext gc) {
        projects.clear();
        gc.allProjects.forEach(project -> {
            if (project.getVersionId() == id) {
                addProject(project);
            }
        });
        projects.forEach(project -> project.initialize(gc));
    }

    public void removeProject(Project project) {
        projects.remove(project);
    }
}
