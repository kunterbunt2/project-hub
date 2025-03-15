package de.bushnaq.abdalla.projecthub.rest.debug;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DebugUtil {
    @Autowired
    ObjectMapper objectMapper;

    public void logJson(Object body) {
        try {
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
            System.out.format("JSON: %s\n", jsonString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
