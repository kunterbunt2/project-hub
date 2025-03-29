package de.bushnaq.abdalla.projecthub.report.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ResourcesUtilization extends HashMap<String, ResourceUtilization> {

    public ResourcesUtilization() {
        super();
    }

    public List<String> getSortedKeyList() {
        List<String> authors = Arrays.asList(this.keySet().toArray(new String[0]));
        return authors;
    }

}
