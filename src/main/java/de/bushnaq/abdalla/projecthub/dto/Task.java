package de.bushnaq.abdalla.projecthub.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

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
public class Task {

    @JsonIgnore
    private List<Task>    childTasks = new ArrayList<>();
    private boolean       critical   = false;
    private Duration      duration   = Duration.ZERO;
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
    @JsonBackReference(value = "sprint-task")
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Sprint         sprint;
    private LocalDateTime  start;
    private TaskMode       taskMode     = TaskMode.AUTO_SCHEDULED;
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
