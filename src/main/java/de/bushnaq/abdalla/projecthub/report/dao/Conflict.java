package de.bushnaq.abdalla.projecthub.report.dao;

import de.bushnaq.abdalla.projecthub.dto.Task;

public class Conflict {
    public boolean originalConflict;
    public String  projectName;
    public Range   range;
    public Task    task;

    public Conflict(String projectName, Task task, Range range, boolean originalConflict) {
        this.projectName      = projectName;
        this.task             = task;
        this.range            = range;
        this.originalConflict = originalConflict;
    }
}
