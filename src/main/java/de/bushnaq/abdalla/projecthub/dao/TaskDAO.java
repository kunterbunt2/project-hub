package de.bushnaq.abdalla.projecthub.dao;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
//@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
public class TaskDAO {


    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
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
    @JsonBackReference
    private TaskDAO parent;

//    List<Relation> predecessors = new ArrayList<>();

    @Column(nullable = false)
    LocalDateTime start;

//    List<Relation> successors = new ArrayList<>();

    boolean isMilestone() {
        return false;
    }

}
