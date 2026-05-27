package az.saglamol.healthrecord.mapper;

import az.saglamol.healthrecord.dto.response.DocumentHashIndexResponse;
import az.saglamol.healthrecord.dto.response.HealthAccessLogResponse;
import az.saglamol.healthrecord.dto.response.HealthRecordResponse;
import az.saglamol.healthrecord.dto.response.MedicalDocumentResponse;
import az.saglamol.healthrecord.dto.response.MedicalDocumentSummaryResponse;
import az.saglamol.healthrecord.dto.response.TreatmentResponse;
import az.saglamol.healthrecord.entity.DocumentHashIndex;
import az.saglamol.healthrecord.entity.HealthAccessLog;
import az.saglamol.healthrecord.entity.HealthRecord;
import az.saglamol.healthrecord.entity.MedicalDocument;
import az.saglamol.healthrecord.entity.Treatment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthRecordMapper {
    HealthRecordResponse toResponse(HealthRecord healthRecord);

    TreatmentResponse toResponse(Treatment treatment);

    MedicalDocumentResponse toResponse(MedicalDocument document);

    MedicalDocumentSummaryResponse toSummaryResponse(MedicalDocument document);

    DocumentHashIndexResponse toResponse(DocumentHashIndex hashIndex);

    HealthAccessLogResponse toResponse(HealthAccessLog accessLog);
}
