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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.bushnaq.abdalla.projecthub.report.dao.WorklogRemaining;
import de.bushnaq.abdalla.projecthub.report.renderer.gantt.GanttContext;
import de.bushnaq.abdalla.util.DurationDeserializer;
import de.bushnaq.abdalla.util.DurationSerializer;
import lombok.*;
import net.sf.mpxj.ProjectCalendar;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Sprint extends AbstractTimeAware implements Comparable<Sprint> {

    @JsonIgnore
    private ProjectCalendar calendar;

    private   LocalDateTime   end;
    @JsonIgnore
    public    List<Throwable> exceptions = new ArrayList<>();
    private   Long            id;
    private   String          name;
    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private   Project         project;
    private   Long            projectId;
    @JsonIgnore
    private   LocalDateTime   releaseDate;//calculated from the task work, worklogs and remaining work
    @JsonSerialize(using = DurationSerializer.class)
    @JsonDeserialize(using = DurationDeserializer.class)
    private   Duration        remaining;
    private   LocalDateTime   start;
    private   Status          status;
    @JsonIgnore
    transient Map<Long, Task> taskMap    = new HashMap<>();
    @JsonIgnore
    private   List<Task>      tasks      = new ArrayList<>();
    private   Long            userId;
    @JsonIgnore
    transient Map<Long, User> userMap    = new HashMap<>();
    @JsonSerialize(using = DurationSerializer.class)
    @JsonDeserialize(using = DurationDeserializer.class)
    private   Duration        worked;
    @JsonIgnore
    List<WorklogRemaining> worklogRemaining = new ArrayList<>();
    @JsonIgnore
    private List<Worklog> worklogs = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void addWorklogRemaining(Task task) {
        Duration timeSpentMinutes         = task.getTimeSpent();
        Duration remainingEstimateMinutes = task.getRemainingEstimate();
        if (timeSpentMinutes.equals(Duration.ZERO) && remainingEstimateMinutes.equals(Duration.ZERO)) {
            return;
        }
        if (timeSpentMinutes == null && remainingEstimateMinutes == null) {
            return;
        }
        if (timeSpentMinutes == null) {
            timeSpentMinutes = Duration.ZERO;
        }
        if (remainingEstimateMinutes == null) {
            remainingEstimateMinutes = Duration.ZERO;
        }
        if (task.getResourceId() != null) {
            WorklogRemaining w = new WorklogRemaining(getId(), task.getId(), task.getKey(), task.getAssignedUser().getName(), timeSpentMinutes, remainingEstimateMinutes);
            worklogRemaining.add(w);
        } else {
            WorklogRemaining w = new WorklogRemaining(getId(), task.getId(), task.getKey(), "unknown", timeSpentMinutes, remainingEstimateMinutes);
            worklogRemaining.add(w);
        }
    }

    @Override
    public int compareTo(Sprint other) {
        return this.id.compareTo(other.id);
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

    @JsonIgnore
    public User getUser() {
        return getuser(userId);
    }

    public User getuser(Long resourceId) {
        return userMap.get(resourceId);
    }

    public void initialize(GanttContext gc) {
        tasks.clear();
        taskMap.clear();
        worklogRemaining.clear();
        //map users to their ids
        gc.allUsers.forEach(user -> userMap.put(user.getId(), user));
        //populate tasks list
        gc.allTasks.forEach(task -> {
            if (task.getSprintId().equals(id)) {
                addTask(task);
            }
        });
        gc.allWorklogs.forEach(worklog -> {
            if (worklog.getSprintId().equals(id)) {
                worklogs.add(worklog);
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
            for (Worklog worklog : worklogs) {
                if (worklog.getTaskId().equals(task.getId())) {
                    task.addWorklog(worklog);
                }
            }
            task.setSprint(this);
            task.initialize();
            addWorklogRemaining(task);
        });
        if (userId == null) calendar = gc.getProjectFile().getDefaultCalendar();
        else {
            calendar = getUser().getCalendar();
        }

    }

    @JsonIgnore
    public boolean isClosed() {
        return getStatus().equals(Status.CLOSED);
    }

}
