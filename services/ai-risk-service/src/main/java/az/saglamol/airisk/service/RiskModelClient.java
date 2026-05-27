package az.saglamol.airisk.service;

import az.saglamol.airisk.model.AiRiskPayload;
import az.saglamol.airisk.model.RiskModelResult;

public interface RiskModelClient {
    RiskModelResult assess(AiRiskPayload payload);
}
