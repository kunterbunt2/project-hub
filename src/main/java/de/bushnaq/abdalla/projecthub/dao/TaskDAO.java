package de.bushnaq.abdalla.projecthub.dao;

import de.bushnaq.abdalla.projecthub.dto.TaskMode;
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


//    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JsonManagedReference(value = "task-task")
//    private List<Long> childTaskIds = new ArrayList<>();

    @Column(nullable = false)
    private boolean critical;

    @Column(nullable = false)
    private Duration duration = Duration.ZERO;

    @Column(nullable = true)
    private LocalDateTime finish;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    //    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "parent_id")
//    @JsonBackReference(value = "task-task")
//    @ToString.Exclude//help intellij debugger not to go into a loop
//    private TaskDAO parentTask;
    @Column(nullable = true)
    private Long parentTaskId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    private List<RelationDAO> predecessors = new ArrayList<>();

    @Column(nullable = false)
    private Number progress;

    @Column(nullable = true)
    private Long resourceId;

    //    @ManyToOne(fetch = FetchType.LAZY)
//    @JsonBackReference(value = "sprint-task")
//    @ToString.Exclude//help intellij debugger not to go into a loop
    @Column(nullable = false)
    private Long sprintId;

    //    List<Relation> successors = new ArrayList<>();
    @Column(nullable = true)
    private LocalDateTime start;

    @Column(nullable = false)
    private TaskMode taskMode;

    @Column(nullable = true)
    private Duration work;

    public boolean isMilestone() {
        return false;
    }

}
