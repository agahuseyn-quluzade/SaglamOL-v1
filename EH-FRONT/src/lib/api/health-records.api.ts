import { apiClient } from "@/lib/api/client";

export type HealthRecordResponse = {
  id: string;
  patientId: string;
  patientName?: string;
  diagnosis?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
};

export type CreateHealthRecordRequest = {
  patientId: string;
  diagnosis?: string;
  notes?: string;
};

export type MedicalDocumentResponse = {
  id: string;
  fileName: string;
  fileType: string;
  uploadedAt?: string;
  url?: string;
};

export const healthRecordsApi = {
  getMyRecords: () => apiClient.get<HealthRecordResponse[]>("/api/v1/health-records/my"),
  getById: (id: string) => apiClient.get<HealthRecordResponse>(`/api/v1/health-records/${id}`),
  create: (data: CreateHealthRecordRequest) => apiClient.post<HealthRecordResponse>("/api/v1/health-records", data),
  getDocuments: (healthRecordId: string) =>
    apiClient.get<MedicalDocumentResponse[]>(`/api/v1/health-records/${healthRecordId}/documents`),
  uploadDocument: (healthRecordId: string, file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return apiClient.post<MedicalDocumentResponse>(
      `/api/v1/health-records/${healthRecordId}/documents`,
      formData,
      { headers: { "Content-Type": "multipart/form-data" } },
    );
  },
};
