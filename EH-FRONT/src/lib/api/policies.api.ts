import { apiClient } from "@/lib/api/client";

export const policiesApi = {
  cancel: (policyId: string, reason?: string) =>
    apiClient.put(`/api/v1/policies/${policyId}/cancel`, null, { params: { reason } }),
  checkEligibility: (data: unknown) => apiClient.post("/api/v1/policies/eligibility-check", data),
  getById: (policyId: string) => apiClient.get(`/api/v1/policies/${policyId}`),
  getMine: () => apiClient.get("/api/v1/policies/me"),
  issue: (data: unknown) => apiClient.post("/api/v1/policies", data),
  search: (params: Record<string, unknown>) => apiClient.get("/api/v1/policies", { params }),
};
