package de.bushnaq.abdalla.projecthub.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @JsonManagedReference
    List<Task> childTasks = new ArrayList<>();
    Duration      duration;
    LocalDateTime finish;
    Long          id;
    String        name;
    //    List<Relation> successors   = new ArrayList<>();
    @JsonBackReference
    private Task parent;

    //    @JsonManagedReference
    List<Relation> predecessors = new ArrayList<>();
    Long           resourceId;
    LocalDateTime  start;

    public void addChildTask(Task childTask) {
        if (childTask.getParent() != null) {
            childTask.getParent().removeChildTask(childTask);
        }
        childTask.setParent(this);
        childTasks.add(childTask);
    }

    public void addDependency(Task dependency) {
        predecessors.add(new Relation(dependency));
    }

    //    public void setDuration(Duration duration) {
//        this.duration = duration;
//        setFinish(getStart().plus(duration));
//    }
    String getKey() {
        return "T-" + id;
    }

    boolean isMilestone() {
        return false;
    }

    public void removeChildTask(Task childTask2) {
        childTasks.remove(childTask2);
    }
}
