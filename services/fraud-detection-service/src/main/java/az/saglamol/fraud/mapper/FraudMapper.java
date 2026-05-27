package az.saglamol.fraud.mapper;

import az.saglamol.fraud.dto.FraudAssessmentResponse;
import az.saglamol.fraud.dto.FraudSignalResponse;
import az.saglamol.fraud.entity.FraudAssessment;
import az.saglamol.fraud.entity.FraudSignal;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FraudMapper {
    FraudAssessmentResponse toResponse(FraudAssessment assessment);

    FraudSignalResponse toResponse(FraudSignal signal);
}
