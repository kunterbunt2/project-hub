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
//@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Task {

    @JsonIgnore
    private List<Task>    childTasks = new ArrayList<>();
    private boolean       critical   = false;
    @JsonSerialize(using = DurationSerializer.class)
    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration      duration;
    private LocalDateTime finish;
    private Long          id;
    //    List<Relation> successors   = new ArrayList<>();
    private String        name;
    @JsonIgnore
    private String        notes;

    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Task parentTask;

    private Long           parentTaskId;
    //    @JsonManagedReference
    private List<Relation> predecessors = new ArrayList<>();
    private Number         progress     = 0;
    private Long           resourceId;
    //    @JsonBackReference(value = "sprint-task")
//    @ToString.Exclude//help intellij debugger not to go into a loop
    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Sprint         sprint;
    private Long           sprintId;
    private LocalDateTime  start;
    private TaskMode       taskMode     = TaskMode.AUTO_SCHEDULED;
    @JsonSerialize(using = DurationSerializer.class)
    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration       work         = Duration.ZERO;

    public void addChildTask(Task childTask) {
        if (childTask.getParentTask() != null) {
            childTask.getParentTask().removeChildTask(childTask);
        }
        childTask.setParentTask(this);
        childTask.setParentTaskId(this.getId());
        childTasks.add(childTask);
    }

    public void addPredecessor(Task dependency) {
        predecessors.add(new Relation(dependency));
    }

    @JsonIgnore
    public User getAssignedUser() {
        return sprint.getuser(resourceId);
    }

    @JsonIgnore
    public ProjectCalendar getEffectiveCalendar() {
        return getSprint().getDefaultCalendar();
    }

    //    public void setDuration(Duration duration) {
//        this.duration = duration;
//        setFinish(getStart().plus(duration));
//    }
    @JsonIgnore
    public String getKey() {
        return "T-" + id;
    }

    public void initialize() {
    }

    @JsonIgnore
    public boolean isMilestone() {
        return work == null || work.isZero();
    }

    public void removeChildTask(Task childTask) {
        childTasks.remove(childTask);
        childTask.setParentTask(null);
        childTask.setParentTaskId(null);
    }
}
