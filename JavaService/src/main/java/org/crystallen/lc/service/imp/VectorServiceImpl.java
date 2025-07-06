package org.crystallen.lc.service.imp;

import lombok.extern.slf4j.Slf4j;
import org.crystallen.lc.service.VectorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VectorServiceImpl implements VectorService {

    @Value("${vector.service.url:http://198.176.62.17}")
    private String vectorServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String vectorizeAndStore(String text, Long userId) {
        try {
            String url = vectorServiceUrl + "/vectorize";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);
            requestBody.put("user_id", userId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("vector_id");
            } else {
                log.error("Vectorization failed: {}", response.getBody());
                throw new RuntimeException("Vectorization failed");
            }
        } catch (Exception e) {
            log.error("Error calling vector service: ", e);
            throw new RuntimeException("Vector service error", e);
        }
    }

    @Override
    public List<Map<String, Object>> searchSimilarMemories(String text, Long userId, Integer limit, Double threshold) {
        try {
            String url = vectorServiceUrl + "/search";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);
            requestBody.put("user_id", userId);
            requestBody.put("limit", limit);
            requestBody.put("threshold", threshold);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (List<Map<String, Object>>) response.getBody().get("memories");
            } else {
                log.error("Search failed: {}", response.getBody());
                throw new RuntimeException("Search failed");
            }
        } catch (Exception e) {
            log.error("Error calling search service: ", e);
            throw new RuntimeException("Search service error", e);
        }
    }

    @Override
    public Map<String, Object> getMemoryById(String memoryId) {
        try {
            String url = vectorServiceUrl + "/memory/" + memoryId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Get memory failed: {}", response.getBody());
                throw new RuntimeException("Get memory failed");
            }
        } catch (Exception e) {
            log.error("Error calling get memory service: ", e);
            throw new RuntimeException("Get memory service error", e);
        }
    }

    @Override
    public List<Map<String, Object>> getUserMemories(Long userId, Integer limit) {
        try {
            String url = vectorServiceUrl + "/memories";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            requestBody.put("limit", limit);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (List<Map<String, Object>>) response.getBody().get("memories");
            } else {
                log.error("Get user memories failed: {}", response.getBody());
                throw new RuntimeException("Get user memories failed");
            }
        } catch (Exception e) {
            log.error("Error calling get user memories service: ", e);
            throw new RuntimeException("Get user memories service error", e);
        }
    }

    @Override
    public boolean deleteMemory(String memoryId) {
        try {
            String url = vectorServiceUrl + "/memory/" + memoryId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, request, Map.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error calling delete memory service: ", e);
            return false;
        }
    }
} 