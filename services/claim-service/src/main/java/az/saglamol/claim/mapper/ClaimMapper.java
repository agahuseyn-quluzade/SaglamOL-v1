package az.saglamol.claim.mapper;

import az.saglamol.claim.dto.response.ClaimDecisionResponse;
import az.saglamol.claim.dto.response.ClaimDocumentReferenceResponse;
import az.saglamol.claim.dto.response.ClaimItemResponse;
import az.saglamol.claim.dto.response.ClaimResponse;
import az.saglamol.claim.dto.response.ClaimSummaryResponse;
import az.saglamol.claim.dto.response.ClaimStatusHistoryResponse;
import az.saglamol.claim.entity.Claim;
import az.saglamol.claim.entity.ClaimDecision;
import az.saglamol.claim.entity.ClaimDocumentReference;
import az.saglamol.claim.entity.ClaimItem;
import az.saglamol.claim.entity.ClaimStatusHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClaimMapper {
    ClaimResponse toResponse(Claim claim);

    ClaimSummaryResponse toSummaryResponse(Claim claim);

    ClaimItemResponse toResponse(ClaimItem item);

    ClaimDocumentReferenceResponse toResponse(ClaimDocumentReference documentReference);

    ClaimDecisionResponse toResponse(ClaimDecision decision);

    ClaimStatusHistoryResponse toResponse(ClaimStatusHistory history);
}
