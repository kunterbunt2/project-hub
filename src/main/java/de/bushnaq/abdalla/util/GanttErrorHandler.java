package de.bushnaq.abdalla.util;


import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.report.dao.MetaData;

public class GanttErrorHandler extends ExcelErrorHandler {
    public static final String ERROR_100_UNSUPPORTED_MULTIPLE_RESOURCES_ASSIGNED  = "Error #100: Unsupported multiple resources assigned to task [#%d] '%s' in Gantt %s.";
    public static final String ERROR_101_UNSUPPORTED_SPLITS                       = "Error #101: Unsupported splits in task [#%d] '%s' in Gantt %s.";
    public static final String ERROR_102_FOUND_RESOURCE_S_BUT_CANNOT_ASSIGN_COLOR = "Error #102: Found resource %s but cannot assign color in task [#%d] '%s' in Gantt %s.";

    public void addComment(Task task, String message) {
        exceptions.add(new ErrorException(message));
        MetaData md = TaskUtil.getTaskMetaData(task);
        //        HashMap<Task, MetaData> mdMap = (HashMap<Task, MetaData>) task.getParentFile().getProjectProperties().getCustomProperties().get(MetaData.METADATA);
        //        MetaData md = mdMap.get(task);
        if (md == null) {
            md = new MetaData();
            TaskUtil.putTaskMetaData(task, md);
        }
        md.addError(message);
    }

    @Override
    protected boolean doubleIsDifferent(double d1, double d2, double delta) {
        return (Double.compare(d1, d2) != 0) && (!(Math.abs(d1 - d2) <= delta));
    }

    public boolean isNotNull(String message, Task task, Object o) {
        if (o == null) {
            noException = false;
            addComment(task, message);
            return false;
        }
        return true;
    }

    public boolean isNull(String message, Task task, Object o) {
        if (o != null) {
            noException = false;
            addComment(task, message);
            return false;
        }
        return true;
    }

    @Override
    public boolean isTrue(String message, boolean value) {
        if (!value) {
            noException = false;
            exceptions.add(new Exception(message));
            return false;
        }
        return true;
    }

    public boolean isTrue(String message, Task task, boolean value) {
        if (!value) {
            noException = false;
            addComment(task, message);
            return false;
        }
        return true;
    }
}
