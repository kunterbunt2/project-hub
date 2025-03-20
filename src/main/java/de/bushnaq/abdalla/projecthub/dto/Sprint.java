package de.bushnaq.abdalla.projecthub.dto;


import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Sprint extends AbstractTimeAware {
    private OffsetDateTime end;
    private Long           id;
    private String         name;
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
