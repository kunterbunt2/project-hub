package de.bushnaq.abdalla.projecthub.rest.debug;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
//            int        rawStatus = ((org.springframework.http.server.ServerHttpResponse) response).getRawStatusCode();
//            HttpStatus status    = HttpStatus.valueOf(rawStatus);

//            HttpStatus status     = HttpStatus.valueOf(response.getStatusCode().value());

            int status = ((ServletServerHttpResponse) response).getServletResponse().getStatus();


//            if (body instanceof ErrorResponse errorResponse)
            if (status != 200) {
                System.out.format("Error Response: %d", status);
            } else {
                String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
                System.out.format("Response JSON: %s\n", jsonString);
            }

//            if (status.is2xxSuccessful()) {
//                logger.info("Response: {} - {}", status, jsonString);
//            } else {
//                logger.error("Error Response: {} - {}", status, jsonString);
//            }

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