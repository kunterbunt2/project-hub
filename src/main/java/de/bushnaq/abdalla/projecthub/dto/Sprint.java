package de.bushnaq.abdalla.projecthub.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

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
    private   OffsetDateTime  end;
    private   Long            id;
    private   String          name;
    //    @ToString.Exclude//help intellij debugger not to go into a loop
//    @JsonBackReference(value = "project-sprint")
    @JsonIgnore
    @ToString.Exclude//help intellij debugger not to go into a loop
    private   Project         project;
    private   Long            projectId;
    private   OffsetDateTime  start;
    private   Status          status;
    @JsonIgnore
    transient Map<Long, Task> taskMap = new HashMap<>();

    @JsonIgnore
//    @JsonManagedReference(value = "sprint-task")
    private List<Task> tasks = new ArrayList<>();

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
                if (latestDate == null || task.getStart().isAfter(latestDate)) {
                    latestDate = task.getStart();
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

    public void initialize(List<User> allUsers, List<Task> allTasks) {
        //map users to their ids
        allUsers.forEach(user -> userMap.put(user.getId(), user));
        //populate tasks list
        allTasks.forEach(task -> {
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
        int a = 0;
    }
}
