package de.bushnaq.abdalla.projecthub.dto;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.bushnaq.abdalla.projecthub.dao.SprintDAO;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@JsonIdentityInfo(
        scope = SprintDAO.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Sprint extends AbstractTimeAware {
    private OffsetDateTime end;
    private Long           id;
    private String         name;
    @ToString.Exclude//help intellij debugger not to go into a loop
    private Project        project;
    private OffsetDateTime start;
    private Status         status;
    private List<Task>     tasks = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    String getKey() {
        return "S-" + id;
    }

}
