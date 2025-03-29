package de.bushnaq.abdalla.projecthub.dto;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @ToString.Exclude//help intellij debugger not to go into a loop
    @JsonBackReference(value = "project-sprint")
    private   Project         project;
    private   OffsetDateTime  start;
    private   Status          status;
    @JsonIgnore
    transient Map<Long, Task> taskMap = new HashMap<>();
    @JsonManagedReference(value = "sprint-task")
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

    public void initialize(List<User> allUsers) {
        allUsers.forEach(user -> userMap.put(user.getId(), user));
        tasks.forEach(task -> taskMap.put(task.getId(), task));
        tasks.forEach(task -> {
            task.setParentTask(taskMap.get(task.getResourceId()));
//            task.setSprint(this);
            task.initialize();
        });
    }
}
