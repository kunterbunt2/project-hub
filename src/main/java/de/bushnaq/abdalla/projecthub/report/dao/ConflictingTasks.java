package de.bushnaq.abdalla.projecthub.report.dao;

import de.bushnaq.abdalla.projecthub.dto.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConflictingTasks {
    private final Map<Task, List<Conflict>> conflictings = new HashMap<>();

    public void add(Task task, Conflict conflict) {
        List<Conflict> list = conflictings.get(task);
        if (list == null) {
            list = new ArrayList<>();
            conflictings.put(task, list);
        }
        list.add(conflict);
    }

    public List<Conflict> get(Task task) {
        return conflictings.get(task);
    }
}
