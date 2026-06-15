import { apiClient } from "@/lib/api/client";

export const passwordApi = {
  // POST /api/v1/iam/password/change  → ChangePasswordRequest
  change: (currentPassword: string, newPassword: string) =>
    apiClient.post("/api/v1/iam/password/change", { currentPassword, newPassword }),
  // POST /api/v1/iam/password/reset-confirm → PasswordResetConfirmRequest { resetToken, newPassword }
  confirmReset: (resetToken: string, newPassword: string) =>
    apiClient.post("/api/v1/iam/password/reset-confirm", { resetToken, newPassword }),
  // POST /api/v1/iam/password/reset-request → PasswordResetRequest { identifier }
  requestReset: (identifier: string) =>
    apiClient.post("/api/v1/iam/password/reset-request", { identifier }),
};
