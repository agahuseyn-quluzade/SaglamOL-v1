import { apiClient } from "@/lib/api/client";

export const profilesApi = {
  agentByCompany: (companyId: string) => apiClient.get(`/api/v1/profiles/agents/by-company/${companyId}`),
  hospitalBranches: (hospitalId: string) => apiClient.get(`/api/v1/profiles/hospitals/${hospitalId}/branches`),
  hospitalDoctors: (hospitalId: string) => apiClient.get(`/api/v1/profiles/hospitals/${hospitalId}/doctors`),
  hospitalStaff: (hospitalId: string) => apiClient.get(`/api/v1/profiles/hospitals/${hospitalId}/staff`),
  patientMe: () => apiClient.get("/api/v1/profiles/patients/me"),
  searchPatients: (q: string) => apiClient.get("/api/v1/profiles/patients/search", { params: { q } }),
};
