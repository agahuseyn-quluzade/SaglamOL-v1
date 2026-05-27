package az.saglamol.airisk.service;

import az.saglamol.airisk.config.AiRiskProperties;
import az.saglamol.airisk.entity.AiRiskAssessmentStatus;
import az.saglamol.airisk.entity.RiskLevel;
import az.saglamol.airisk.model.AiRiskPayload;
import az.saglamol.airisk.model.RiskModelResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ExternalAiRiskModelClient implements RiskModelClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AiPromptBuilder promptBuilder;
    private final AiRiskProperties properties;

    public ExternalAiRiskModelClient(RestClient aiRiskRestClient, ObjectMapper objectMapper,
                                     AiPromptBuilder promptBuilder, AiRiskProperties properties) {
        this.restClient = aiRiskRestClient;
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
        this.properties = properties;
    }

    @Override
    public RiskModelResult assess(AiRiskPayload payload) {
        Map<String, Object> request = Map.of(
                "model", properties.getModelName(),
                "temperature", 0,
                "messages", List.of(
                        Map.of("role", "system", "content", promptBuilder.systemPrompt()),
                        Map.of("role", "user", "content", write(payload))
                )
        );
        String response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .body(request)
                .retrieve()
                .body(String.class);
        return parse(response);
    }

    private RiskModelResult parse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText(response);
            JsonNode result = objectMapper.readTree(stripCodeFence(content));
            BigDecimal score = decimal(result.path("riskScore").asText("0"));
            BigDecimal confidence = decimal(result.path("confidence").asText("0.75"));
            RiskLevel level = RiskLevel.valueOf(result.path("riskLevel").asText(level(score).name()).toUpperCase());
            List<String> reasons = new ArrayList<>();
            result.path("reasons").forEach(reason -> reasons.add(reason.asText()));
            if (reasons.isEmpty()) {
                reasons.add("External model returned no reason details");
            }
            return new RiskModelResult(score, level, confidence, reasons, response, AiRiskAssessmentStatus.SUCCESS,
                    properties.getProviderName(), properties.getModelName(), response, null);
        } catch (RuntimeException | JsonProcessingException exception) {
            throw new IllegalStateException("Failed to parse AI provider response", exception);
        }
    }

    private String stripCodeFence(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```")) {
            return trimmed.replaceFirst("^```json\\s*", "").replaceFirst("^```\\s*", "").replaceFirst("\\s*```$", "");
        }
        return trimmed;
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value).max(BigDecimal.ZERO).min(BigDecimal.ONE).setScale(4, RoundingMode.HALF_UP);
    }

    private RiskLevel level(BigDecimal score) {
        if (score.compareTo(new BigDecimal("0.8000")) >= 0) {
            return RiskLevel.CRITICAL;
        }
        if (score.compareTo(new BigDecimal("0.5000")) >= 0) {
            return RiskLevel.HIGH;
        }
        if (score.compareTo(new BigDecimal("0.2500")) >= 0) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize AI risk payload", exception);
        }
    }
}
