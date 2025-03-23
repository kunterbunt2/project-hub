package de.bushnaq.abdalla.projecthub.dto;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private OffsetDateTime end;
    private Long           id;
    private String         name;


    @ToString.Exclude//help intellij debugger not to go into a loop
    @JsonBackReference(value = "project-sprint")
    private Project        project;
    private OffsetDateTime start;
    private Status         status;

    @JsonManagedReference(value = "sprint-task")
    private List<Task> tasks = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

//    public void addTask(Task task) {
//        tasks.add(task);
//    }

    String getKey() {
        return "S-" + id;
    }

}
