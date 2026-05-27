package az.saglamol.airisk.mapper;

import az.saglamol.airisk.dto.AiRiskAssessmentResponse;
import az.saglamol.airisk.entity.AiRiskAssessment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AiRiskMapper {
    AiRiskAssessmentResponse toResponse(AiRiskAssessment assessment);
}
