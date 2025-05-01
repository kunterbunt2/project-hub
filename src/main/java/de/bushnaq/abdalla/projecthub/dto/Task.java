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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.bushnaq.abdalla.util.DurationDeserializer;
import de.bushnaq.abdalla.util.DurationSerializer;
import lombok.*;
import net.sf.mpxj.ProjectCalendar;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * represents a task in a Gantt chart.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true)
public class Task implements Comparable<Task> {

    @JsonIgnore
    private List<Task>    childTasks = new ArrayList<>();
    private boolean       critical   = false;
    @JsonSerialize(using = DurationSerializer.class)
    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration      duration;
    private LocalDateTime finish;
    private Long          id;
    private boolean       milestone;
    private String        name;
    @JsonIgnore
    private String        notes;
    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Task          parentTask;

    private Long           parentTaskId;
    private List<Relation> predecessors = new ArrayList<>();
    private Number         progress     = 0;
    private Long           resourceId;
    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Sprint         sprint;
    private Long           sprintId;
    private LocalDateTime  start;
    private TaskMode       taskMode     = TaskMode.AUTO_SCHEDULED;
    @JsonSerialize(using = DurationSerializer.class)
    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration       work         = Duration.ZERO;
    @JsonIgnore
    private List<Worklog>  worklogs     = new ArrayList<>();

    public void addChildTask(Task childTask) {
        if (childTask.getParentTask() != null) {
            childTask.getParentTask().removeChildTask(childTask);
        }
        childTask.setParentTask(this);
        childTask.setParentTaskId(this.getId());
        childTasks.add(childTask);
    }

    public void addPredecessor(Task dependency, boolean isVisible) {
        predecessors.add(new Relation(dependency, isVisible));
    }

    public void addWorklog(Worklog worklog) {
        worklog.setTaskId(id);
        worklogs.add(worklog);
    }

    @Override
    public int compareTo(Task other) {
        return this.id.compareTo(other.id);
    }

    @JsonIgnore
    public User getAssignedUser() {
        return sprint.getuser(resourceId);
    }

    @JsonIgnore
    public ProjectCalendar getEffectiveCalendar() {
        User user = getAssignedUser();
        if (user != null) {
            return user.getCalendar();
        } else {
            return sprint.getCalendar();
        }
    }

    @JsonIgnore
    public String getKey() {
        return "T-" + id;
    }

    public void initialize() {
    }

    public void removeChildTask(Task childTask) {
        childTasks.remove(childTask);
        childTask.setParentTask(null);
        childTask.setParentTaskId(null);
    }
}
