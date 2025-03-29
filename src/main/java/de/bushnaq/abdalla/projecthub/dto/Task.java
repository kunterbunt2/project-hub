package de.bushnaq.abdalla.projecthub.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @JsonManagedReference(value = "task-task")
    private List<Task>     childTasks   = new ArrayList<>();
    private boolean        critical     = false;
    private Duration       duration;
    private LocalDateTime  finish;
    private Long           id;
    //    List<Relation> successors   = new ArrayList<>();
    private String         name;
    @JsonIgnore
    private String         notes;
    @JsonBackReference(value = "task-task")
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Task           parentTask;
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
        return duration == null || duration.isZero();
    }

    public void removeChildTask(Task childTask2) {
        childTasks.remove(childTask2);
    }

}
