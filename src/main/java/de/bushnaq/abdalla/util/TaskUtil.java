package de.bushnaq.abdalla.util;

import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.report.dao.MetaData;

import java.util.HashMap;


public class TaskUtil {

    public static HashMap<Task, MetaData> createMetaData(Sprint projectFile) {
        //TODO fix this
//        ProjectProperties properties = projectFile.getProjectProperties();
//        if (properties.getCustomProperties() == null || properties.getCustomProperties().get(MetaData.METADATA) == null) {
//            Map<String, Object>     customProperties = new HashMap<>();
//            HashMap<Task, MetaData> mdMap            = new HashMap<>();
//            customProperties.put(MetaData.METADATA, mdMap);
//            properties.setCustomProperties(customProperties);
//            return mdMap;
//        } else {
//            return (HashMap<Task, MetaData>) projectFile.getProjectProperties().getCustomProperties().get(MetaData.METADATA);
//        }
        return null;
    }

    public static MetaData getTaskMetaData(Task task) {
//        HashMap<Task, MetaData> mdMap = createMetaData(task.getParentFile());
//        return mdMap.get(task);
        return null;
    }

    public static void putTaskMetaData(Task task, MetaData md) {
//        HashMap<Task, MetaData> mdMap = createMetaData(task.getParentFile());
//        mdMap.put(task, md);
    }
}
