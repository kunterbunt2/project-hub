package de.bushnaq.abdalla.projecthub.rest.debug;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.rest.util.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class JsonResponseLogger implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(JsonResponseLogger.class);
    @Autowired
    ObjectMapper objectMapper;


    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, org.springframework.http.MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            int status = ((ServletServerHttpResponse) response).getServletResponse().getStatus();
            if (status != 200) {
                if (body instanceof ErrorResponse errorResponse) {
                    System.out.format("Error Response: %s\n", errorResponse.getMessage());
                } else {
                    System.out.format("Error Response: %d\n", status);
                }
            } else {
                String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
                System.out.format("Response JSON: %s\n", jsonString);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return body;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }
}