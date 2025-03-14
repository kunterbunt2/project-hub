package de.bushnaq.abdalla.projecthub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsonMapper {

    @Autowired
    private ObjectMapper objectMapper;

    public <T> T convert(Object source, Class<T> targetClass) {
        try {
            String json = objectMapper.writeValueAsString(source);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.registerModule(new Jdk8Module());
            return objectMapper.readValue(json, targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Conversion error", e);
        }
    }
}