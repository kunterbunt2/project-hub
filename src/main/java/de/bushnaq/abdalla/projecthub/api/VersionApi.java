package de.bushnaq.abdalla.projecthub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bushnaq.abdalla.projecthub.dto.Version;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class VersionApi extends AbstractApi {

    public VersionApi(RestTemplate restTemplate, ObjectMapper objectMapper, String baseUrl) {
        super(restTemplate, objectMapper, baseUrl);
    }

    public VersionApi() {
    }

    public VersionApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void deleteById(Long id) {
        executeWithErrorHandling(() -> restTemplate.delete(
                baseUrl + "/version/{id}",
                id
        ));
    }

    public List<Version> getAllVersions() {

        ResponseEntity<Version[]> response = executeWithErrorHandling(() -> restTemplate.getForEntity(
                baseUrl + "/version",
                Version[].class
        ));
        return Arrays.asList(response.getBody());
    }

    public Version getVersion(Long id) {
        return executeWithErrorHandling(() ->
                restTemplate.getForObject(
                        baseUrl + "/version/{id}",
                        Version.class,
                        id
                ));
    }

    public Version persist(Version version, Long productId) {
        return executeWithErrorHandling(() ->
                restTemplate.postForObject(
                        baseUrl + "/version/{productId}",
                        version,
                        Version.class,
                        productId
                ));
    }

    public void update(Version user) {
        executeWithErrorHandling(() -> restTemplate.put(
                baseUrl + "/version",
                user
        ));
    }

}