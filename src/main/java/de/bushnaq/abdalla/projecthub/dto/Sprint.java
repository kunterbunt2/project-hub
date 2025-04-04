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
import de.bushnaq.abdalla.projecthub.gantt.GanttContext;
import lombok.*;
import net.sf.mpxj.ProjectCalendar;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
//@JsonIdentityInfo(
//        scope = SprintDAO.class,
//        generator = ObjectIdGenerators.PropertyGenerator.class,
//        property = "id")
public class Sprint extends AbstractTimeAware {

    @JsonIgnore
    private ProjectCalendar defaultCalendar;

    private OffsetDateTime end;
    private Long           id;
    private String         name;
    //    @ToString.Exclude//help intellij debugger not to go into a loop
//    @JsonBackReference(value = "project-sprint")
    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Project        project;

    private   Long            projectId;
    private   OffsetDateTime  start;
    private   Status          status;
    @JsonIgnore
    transient Map<Long, Task> taskMap = new HashMap<>();
    @JsonIgnore
//    @JsonManagedReference(value = "sprint-task")
    private   List<Task>      tasks   = new ArrayList<>();
    @JsonIgnore
    transient Map<Long, User> userMap = new HashMap<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    @JsonIgnore
    public LocalDateTime getEarliestStartDate() {
        LocalDateTime earliestDate = null;
        for (Task task : getTasks()) {
            if (!task.isMilestone() && (task.getChildTasks().isEmpty()) && (task.getDuration() != null && !task.getDuration().isZero())) {
                if (earliestDate == null || task.getStart().isBefore(earliestDate)) {
                    earliestDate = task.getStart();
                }
            } else {
                //ignore milestones
            }
        }
        return earliestDate;
    }

    String getKey() {
        return "S-" + id;
    }

    @JsonIgnore
    public LocalDateTime getLatestFinishDate() {
        LocalDateTime latestDate = null;
        for (Task task : getTasks()) {
            if (!task.isMilestone() && (task.getChildTasks().isEmpty()) && (task.getDuration() != null && !task.getDuration().isZero())) {
                if (latestDate == null || task.getFinish().isAfter(latestDate)) {
                    latestDate = task.getFinish();
                }
            } else {
                //ignore milestones
            }
        }
        return latestDate;
    }

    public Task getTaskById(Long predecessorId) {
        return tasks.stream().filter(task -> task.getId().equals(predecessorId)).findFirst().orElse(null);
    }

    public User getuser(Long resourceId) {
        return userMap.get(resourceId);
    }

    public void initialize(GanttContext gc) {
        //map users to their ids
        gc.allUsers.forEach(user -> userMap.put(user.getId(), user));
        //populate tasks list
        gc.allTasks.forEach(task -> {
            if (task.getSprintId().equals(id)) {
                addTask(task);
            }
        });
        //map tasks to their ids
        tasks.forEach(task -> taskMap.put(task.getId(), task));
        tasks.forEach(task -> {
            //set the parent task
            if (task.getParentTaskId() != null) {
                task.setParentTask(taskMap.get(task.getParentTaskId()));
                //add the task to the parent task
                task.getParentTask().addChildTask(task);
            }
            task.setSprint(this);
            task.initialize();
        });
        defaultCalendar = gc.getProjectFile().getDefaultCalendar();

    }
}
