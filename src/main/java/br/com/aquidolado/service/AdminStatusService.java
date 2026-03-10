package br.com.aquidolado.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminStatusService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.admin.vercel-token:}")
    private String vercelToken;

    @Value("${app.admin.vercel-project-id:}")
    private String vercelProjectId;

    @Value("${app.admin.railway-status-url:}")
    private String railwayStatusUrl;

    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();

        result.put("backend", "ok");
        result.put("backendDetails", "UP");

        if (vercelToken != null && !vercelToken.isBlank() && vercelProjectId != null && !vercelProjectId.isBlank()) {
            result.put("vercel", fetchVercelStatus());
        } else {
            result.put("vercel", Map.of("status", "n/a", "message", "Configure VERCEL_TOKEN e VERCEL_PROJECT_ID"));
        }

        if (railwayStatusUrl != null && !railwayStatusUrl.isBlank()) {
            result.put("railway", fetchRailwayStatus());
        } else {
            result.put("railway", Map.of("status", "n/a", "message", "Configure RAILWAY_STATUS_URL para verificar"));
        }

        return result;
    }

    private Object fetchVercelStatus() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(vercelToken);
            ResponseEntity<String> res = restTemplate.exchange(
                    "https://api.vercel.com/v6/deployments?projectId=" + vercelProjectId + "&limit=1",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            JsonNode root = objectMapper.readTree(res.getBody());
            if (root.has("deployments") && root.get("deployments").isArray() && root.get("deployments").size() > 0) {
                String state = root.get("deployments").get(0).path("state").asText("UNKNOWN");
                String status = "READY".equals(state) ? "ok" : "BUILDING".equals(state) || "INITIALIZING".equals(state) ? "building" : "degraded";
                return Map.of("status", status, "state", state);
            }
            return Map.of("status", "unknown", "message", "Nenhum deployment encontrado");
        } catch (Exception e) {
            log.debug("Vercel status check failed: {}", e.getMessage());
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    private Object fetchRailwayStatus() {
        try {
            ResponseEntity<String> res = restTemplate.getForEntity(railwayStatusUrl, String.class);
            if (res.getStatusCode().is2xxSuccessful()) {
                return Map.of("status", "ok");
            }
            return Map.of("status", "degraded", "code", res.getStatusCodeValue());
        } catch (Exception e) {
            log.debug("Railway status check failed: {}", e.getMessage());
            return Map.of("status", "error", "message", e.getMessage());
        }
    }
}
