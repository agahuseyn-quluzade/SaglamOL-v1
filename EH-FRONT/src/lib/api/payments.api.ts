import { apiClient } from "@/lib/api/client";

export const paymentsApi = {
  byCompany: (companyId: string) => apiClient.get("/api/v1/payments/by-company", { params: { companyId } }),
  byPolicy: (policyId: string) => apiClient.get("/api/v1/payments/by-policy", { params: { policyId } }),
  claimPayout: (data: unknown) => apiClient.post("/api/v1/payments/claim-payout", data),
  mine: () => apiClient.get("/api/v1/payments/my"),
};
