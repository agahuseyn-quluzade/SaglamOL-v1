import { apiClient } from "@/lib/api/client";

export const adminApi = {
  companies: () => apiClient.get("/api/v1/insurance-companies"),
  hospitals: () => apiClient.get("/api/v1/profiles/hospitals"),
  notificationTemplates: () => apiClient.get("/api/v1/notifications/templates"),
  searchUsers: (q: string) => apiClient.get("/api/v1/iam/users/search", { params: { q } }),
  updateUserStatus: (userId: string, status: string) =>
    apiClient.patch(`/api/v1/iam/users/${userId}/status`, { status }),
  users: () => apiClient.get("/api/v1/iam/users"),
};
