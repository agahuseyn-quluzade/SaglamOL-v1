import { apiClient } from "@/lib/api/client";
import type {
  AttachClaimDocumentRequest,
  ClaimItemRequest,
  ClaimItemResponse,
  ClaimResponse,
  ClaimSearchFilters,
  ClaimSummaryResponse,
  CreateClaimRequest,
  Page,
  ReviewClaimRequest,
} from "@/lib/api/types";

export const claimsApi = {
  addItem: (claimId: string, data: ClaimItemRequest) =>
    apiClient.post<ClaimItemResponse>(`/api/v1/claims/${claimId}/items`, data),
  attachDocument: (claimId: string, data: AttachClaimDocumentRequest) =>
    apiClient.post(`/api/v1/claims/${claimId}/documents`, data),
  create: (data: CreateClaimRequest) => apiClient.post<ClaimResponse>("/api/v1/claims", data),
  getById: (claimId: string) => apiClient.get<ClaimResponse>(`/api/v1/claims/${claimId}`),
  getMyClaims: (page = 0, size = 20) =>
    apiClient.get<Page<ClaimSummaryResponse>>("/api/v1/claims/my", { params: { page, size } }),
  search: (filters: ClaimSearchFilters) =>
    apiClient.get<Page<ClaimSummaryResponse>>("/api/v1/claims", { params: filters }),
  submit: (claimId: string) => apiClient.post<ClaimResponse>(`/api/v1/claims/${claimId}/submit`),
  review: {
    approve: (claimId: string, data: ReviewClaimRequest) =>
      apiClient.post<ClaimResponse>(`/api/v1/claims/${claimId}/review/approve`, data),
    cancel: (claimId: string, reason?: string) =>
      apiClient.put<ClaimResponse>(`/api/v1/claims/${claimId}/review/cancel`, null, { params: { reason } }),
    reject: (claimId: string, data: ReviewClaimRequest) =>
      apiClient.post<ClaimResponse>(`/api/v1/claims/${claimId}/review/reject`, data),
    requestMoreDocuments: (claimId: string, reason: string) =>
      apiClient.post<ClaimResponse>(`/api/v1/claims/${claimId}/review/more-documents`, null, {
        params: { reason },
      }),
    retryPayout: (claimId: string) => apiClient.post<ClaimResponse>(`/api/v1/claims/${claimId}/retry-payout`),
    start: (claimId: string) => apiClient.post<ClaimResponse>(`/api/v1/claims/${claimId}/review/start`),
    resubmit: (claimId: string) => apiClient.post<ClaimResponse>(`/api/v1/claims/${claimId}/resubmit`),
  },
};
