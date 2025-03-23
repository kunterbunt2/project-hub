package de.bushnaq.abdalla.projecthub.dao;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * represents a task in a Gantt chart.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
public class TaskDAO {


    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference(value = "task-task")
    List<TaskDAO> childTasks = new ArrayList<>();

    @Column(nullable = false)
    Duration duration;

    @Column(nullable = false)
    LocalDateTime finish;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(nullable = false)
    String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @JsonBackReference(value = "task-task")
    @ToString.Exclude//help intellij debugger not to go into a loop
    private TaskDAO parent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    List<RelationDAO> predecessors = new ArrayList<>();

    @Column(nullable = true)
    Long resourceId;

    //    List<Relation> successors = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference(value = "sprint-task")
    @ToString.Exclude//help intellij debugger not to go into a loop
    SprintDAO sprint;

    @Column(nullable = false)
    LocalDateTime start;

    boolean isMilestone() {
        return false;
    }

}
