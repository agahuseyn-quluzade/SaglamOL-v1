package az.saglamol.fraud.dto;

import java.util.List;

public record FraudAssessmentDetailResponse(
        FraudAssessmentResponse assessment,
        List<FraudSignalResponse> signals
) {
}
